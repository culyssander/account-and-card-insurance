package com.santander.msanalysisservices.services;

import com.santander.msanalysisservices.clients.ClaimServicesClients;
import com.santander.msanalysisservices.clients.UserServicesClients;
import com.santander.msanalysisservices.constants.AnalysisConstants;
import com.santander.msanalysisservices.dto.AnalysisRequestDto;
import com.santander.msanalysisservices.dto.AnalysisResponseDto;
import com.santander.msanalysisservices.dto.ClaimRequestStatusDto;
import com.santander.msanalysisservices.dto.ClaimResponseDto;
import com.santander.msanalysisservices.dto.UserResponseDto;
import com.santander.msanalysisservices.exception.BadRequestException;
import com.santander.msanalysisservices.exception.BusinessException;
import com.santander.msanalysisservices.exception.NotFoundException;
import com.santander.msanalysisservices.model.Analysis;
import com.santander.msanalysisservices.model.AnalysisResult;
import com.santander.msanalysisservices.repository.AnalysisRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class AnalysisServices {

    private ModelMapper modelMapper;
    private RabbitTemplate rabbitTemplate;
    private AnalysisRepository analysisRepository;
    private UserServicesClients userServicesClients;
    private ClaimServicesClients claimServicesClients;

    @Transactional
    public AnalysisResponseDto save(AnalysisRequestDto request) {
        UserResponseDto userLogged = validateUserWithRoleAnalyst();
        String claimId = request.getClaimId();

        validateClaimIdAlreadyExists(claimId);
        validateResultEqualsDenied(request);

        AnalysisResult result = request.getResult();
        Analysis analysis = getAnalysis(request, userLogged.getId());

        analysis = analysisRepository.save(analysis);
        updateClaimStatus(claimId, ClaimRequestStatusDto.builder()
                .status(result.name())
                .build());

        AnalysisResponseDto analysisResponseDto = entityToDto(analysis);
        publishInRabbit(analysisResponseDto);
        return analysisResponseDto;
    }

    private Analysis getAnalysis(AnalysisRequestDto request, BigInteger userLoggedId) {
        return Analysis.builder()
                .claimId(request.getClaimId())
                .analystId(userLoggedId)
                .result(request.getResult())
                .reasonForDenial(request.getReasonForDenial())
                .compensationAmount(request.getCompensationAmount())
                .analysisDate(getLocalDateTime(request.getAnalysisDate()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public List<AnalysisResponseDto> findAll() {
        return analysisRepository.findAll().stream()
                .map(this::entityToDto)
                .toList();
    }

    public AnalysisResponseDto findByClaimIdDto(String claimId) {
        return entityToDto(findByClaimId(claimId));
    }

    private Analysis findByClaimId(String claimId) {
        return analysisRepository.findByClaimId(claimId)
                .orElseThrow(() -> new NotFoundException("Analysis not found"));
    }

    private AnalysisResponseDto entityToDto(Analysis analysis) {
        return modelMapper.map(analysis, AnalysisResponseDto.class);
    }

    private ClaimResponseDto findApiClaimId(String claimId) {
        try {
            return claimServicesClients.findByClaimId(claimId);
        } catch (BusinessException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    private void updateClaimStatus(String claimId, ClaimRequestStatusDto requestStatus) {
        try {
            claimServicesClients.updateStatus(claimId, requestStatus);
        } catch (BusinessException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    private UserResponseDto validateUserWithRoleAnalyst() {
        try {
            UserResponseDto userLogged = userServicesClients.findByUserLogged();

            if (!userLogged.getRole().equals("ANALYST"))
                throw new BusinessException("Not permission");

            return userLogged;
        } catch (BusinessException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    private LocalDateTime getLocalDateTime(String dateTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            return LocalDateTime.parse(dateTime, formatter);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private void publishInRabbit(AnalysisResponseDto analysis) {
        try {
            rabbitTemplate.convertAndSend(AnalysisConstants.RABBIT_QUEUE_ANALYSIS_EXCHANGE,
                    AnalysisConstants.RABBIT_QUEUE_ANALYSIS_ROUTER, analysis);
        } catch (BusinessException e) {
            log.error("Error {} ", e.getMessage());
            throw new BusinessException(e.getMessage());
        }
    }

    private void validateResultEqualsDenied(AnalysisRequestDto request) {
        if (request.getAnalysisDate().equals(AnalysisResult.DENIED.name()) &&
                Objects.isNull(request.getReasonForDenial())) {
            throw new BadRequestException("When a request is denied, you must provide a reason.");
        }
    }

    private void validateClaimIdAlreadyExists(String claimId) {
        if (analysisRepository.existsByClaimId(claimId)) {
            throw new BadRequestException("Claim already exists.");
        }
    }
}
