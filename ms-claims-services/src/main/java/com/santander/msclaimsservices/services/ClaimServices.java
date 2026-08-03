package com.santander.msclaimsservices.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santander.msclaimsservices.clients.PolicyServicesClients;
import com.santander.msclaimsservices.clients.ProtocolSequencialClients;
import com.santander.msclaimsservices.clients.UserServicesClients;
import com.santander.msclaimsservices.constants.ClaimConstants;
import com.santander.msclaimsservices.dto.ClaimRequestDto;
import com.santander.msclaimsservices.dto.ClaimRequestStatusDto;
import com.santander.msclaimsservices.dto.ClaimResponseDto;
import com.santander.msclaimsservices.dto.PolicyResponseDto;
import com.santander.msclaimsservices.exception.BadRequestException;
import com.santander.msclaimsservices.exception.BusinessException;
import com.santander.msclaimsservices.exception.NotFoundException;
import com.santander.msclaimsservices.model.Attachment;
import com.santander.msclaimsservices.model.Claim;
import com.santander.msclaimsservices.model.ClaimStatus;
import com.santander.msclaimsservices.repository.ClaimRepository;
import com.santander.msclaimsservices.util.FileValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ClaimServices {

    private Validator validator;
    private S3Servicos s3Servicos;
    private MessageSource messageSource;
    private RabbitTemplate rabbitTemplate;
    private ClaimRepository claimRepository;
    private AttachmentServices attachmentServices;
    private UserServicesClients userServicesClients;
    private PolicyServicesClients policyServicesClients;
    private ProtocolSequencialClients protocolSequencialClients;

    @Transactional
    public ClaimResponseDto newClaimDto(List<MultipartFile> files, String reclamacao, Locale locale) {
        try {
            validateFiles(files);
            ClaimRequestDto requestDto = inputToDTO(reclamacao, ClaimRequestDto.class);
            ClaimResponseDto claim = newClaim(requestDto, locale);
            List<Attachment> attachments = newAttachment(files, claim.getClaimNumber(), claim.getId());
            publishInRabbit(claim, locale);
            log.info("CREATE CLAIM: {}", claim);
            return claim;
        } catch (BusinessException e) {
            log.error("ERROR TO CREATE CLAIM: {}", e.getMessage());
            String message = messageSource.getMessage(ClaimConstants.CLAIM_ERROR_SAVE_ROLLBACK, new Object[] {e.getMessage()}, locale);
            log.error(message);
            throw new BusinessException(message);
        }
    }

    private ClaimResponseDto newClaim(ClaimRequestDto dto, Locale locale) {
        try {
            PolicyResponseDto policy = getPolicy(dto.getPolicyNumber(), locale);
            String protocol = getProtocolo(locale);

            Claim claim = Claim.builder()
                    .claimNumber(protocol)
                    .status(ClaimStatus.OPEN.name())
                    .description(dto.getDescription())
                    .eventDate(getLocalDateTime(dto.getEventDate()))
                    .openingDate(getLocalDateTime(dto.getOpeningDate()))
                    .claimedAmount(dto.getClaimedAmount())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            claim = claimRepository.save(claim);

            return entityToDto(claim, policy);
        } catch (BusinessException e) {
            throw new BusinessException(messageSource.getMessage(ClaimConstants.CLAIM_ERROR_SAVE, new Object[] {e.getMessage()}, locale));
        }
    }

    private PolicyResponseDto getPolicy(String policyNumber, Locale locale) {
        try {
            return policyServicesClients.findByPolicyNumber(policyNumber);
        } catch (BusinessException e) {
            throw new BusinessException(messageSource.getMessage(ClaimConstants.POLICY_ERROR_TO_FIND, new Object[]{}, locale));
        }
    }

    private String getProtocolo(Locale locale) {
        try {
            String PREFIXO = "SIN";
            return protocolSequencialClients.generateProtocol(PREFIXO);
        } catch (BusinessException e) {
            throw new BusinessException(messageSource.getMessage(ClaimConstants.PROTOCOL_ERROR_TO_GET, new Object[]{}, locale));
        }
    }

    private ClaimResponseDto entityToDto(Claim claim, PolicyResponseDto policy) {
        return ClaimResponseDto.builder()
                .id(claim.getId())
                .policy(policy)
                .claimNumber(claim.getClaimNumber())
                .status(claim.getStatus())
                .description(claim.getDescription())
                .eventDate(claim.getEventDate())
                .openingDate(claim.getOpeningDate())
                .claimedAmount(claim.getClaimedAmount())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    private Claim findByClaimNumber(String claimNumber, Locale locale) {
        return claimRepository.findByClaimNumber(claimNumber)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage(ClaimConstants.CLAIM_NOT_FOUND, new Object[]{ claimNumber }, locale)
                ));
    }

    public ClaimResponseDto findByClaimNumberDto(String claimNumber, Locale locale) {
        Claim claim = findByClaimNumber(claimNumber, locale);
        ClaimResponseDto claimResponseDto = entityToDto(claim, null);

        log.info("FIND CLAIM BY claimNumber: {}", claimResponseDto);

        return claimResponseDto;
    }

    private void publishInRabbit(ClaimResponseDto claim, Locale locale) {
        try {
            rabbitTemplate.convertAndSend(ClaimConstants.RABBIT_QUEUE_CLAIM_EXCHANGE,
                    ClaimConstants.RABBIT_QUEUE_CLAIM_ROUTER, claim);
        } catch (BusinessException e) {
            log.error("Error {} ", e.getMessage());
            throw new BusinessException(messageSource.getMessage(ClaimConstants.CLAIM_ERROR_SAVE, new Object[] {e.getMessage()}, locale));
        }
    }

    private <T> T inputToDTO(String input, Class<T> classe) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            T dto = objectMapper.readValue(input, classe);

            Set<ConstraintViolation<T>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                String mensagens = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                throw new BadRequestException(mensagens);
            }
            return dto;
        } catch (IOException e) {
            throw new BadRequestException("Input invalido: " + e.getMessage());
        }
    }

    private List<Attachment> newAttachment(List<MultipartFile> files, String protocolo, BigInteger reclamacaoId) {
        if (Objects.isNull(files) || files.isEmpty()) {
            return Collections.emptyList();
        }

        Claim claim = Claim.builder().id(reclamacaoId).build();

        List<Attachment> attachments = files.stream()
                .map(file -> createAttachment(file, protocolo, claim))
                .toList();

        return attachmentServices.newAttachment(attachments);
    }

    private Attachment createAttachment(MultipartFile file, String protocolo, Claim claim) {
        try {
            String key = s3Servicos.upload(file, protocolo);
            return Attachment.builder()
                    .claimId(claim.getId())
                    .fileName(protocolo)
                    .documentType(file.getContentType())
                    .url(key)
                    .size(file.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .build();
        } catch (IOException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    private void validateFiles(List<MultipartFile> files) {
        if (Objects.nonNull(files) && !files.isEmpty()) {
            files.forEach(file -> {
                try {
                    FileValidator.validate(file);
                } catch (IOException e) {
                    throw new BusinessException("Invalid image");
                }
            });
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

    @Transactional
    public ClaimResponseDto updateStatusDocOrReview(String claimId,
                                                    ClaimRequestStatusDto status,
                                                    Locale locale) {
        return updateStatus(claimId, status, locale, true);
    }

    @Transactional
    public ClaimResponseDto updateStatusApprovedOrDenied(String claimId,
                                                         ClaimRequestStatusDto status,
                                                         Locale locale) {
        return updateStatus(claimId, status, locale, false);
    }

    private ClaimResponseDto updateStatus(String claimId,
                                          ClaimRequestStatusDto status,
                                          Locale locale,
                                          boolean validateStatus) {
        try {
            Claim claim = findByClaimNumber(claimId, locale);
            userServicesClients.findByUserLogged();

            ClaimStatus claimStatus = getClaimStatus(status.getStatus());

            if (validateStatus) {
                validateClaimStatus(claimStatus);
            }

            claim.setStatus(claimStatus.name());
            claim.setUpdatedAt(LocalDateTime.now());

            Claim savedClaim = claimRepository.save(claim);
            ClaimResponseDto response = entityToDto(savedClaim, null);

            publishInRabbit(response, locale);
            log.info("UPDATE CLAIM STATUS: {}", response);

            return response;

        } catch (BusinessException e) {
            String message = "Error to update status: " + e.getMessage();
            log.error(message, e);
            throw new BadRequestException(message);
        }
    }

    private ClaimStatus getClaimStatus(String status) {
        try {
            return ClaimStatus.valueOf(status);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private void validateClaimStatus(ClaimStatus status) {
        if (status != ClaimStatus.DOCUMENTATION_PENDING && status != ClaimStatus.IN_REVIEW) {
            throw new BadRequestException(
                    "Invalid Status: Update only DOCUMENTATION_PENDING or IN_REVIEW"
            );
        }
    }
}
