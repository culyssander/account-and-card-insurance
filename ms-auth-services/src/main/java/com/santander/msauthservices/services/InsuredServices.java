package com.santander.msauthservices.services;

import com.santander.msauthservices.constants.UserConstants;
import com.santander.msauthservices.dto.InsuredResponseDto;
import com.santander.msauthservices.dto.UserRequestDto;
import com.santander.msauthservices.exception.AlreadyExistsException;
import com.santander.msauthservices.exception.NotFoundException;
import com.santander.msauthservices.model.Insured;
import com.santander.msauthservices.repository.InsuredRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@AllArgsConstructor
public class InsuredServices {

    private InsuredRepository insuredRepository;
    private MessageSource messageSource;
    private ModelMapper modelMapper;

    public InsuredResponseDto newInsured(UserRequestDto request, Locale locale) {
        validateUserByCPF(request.getCpf(), locale);

        Insured insured = Insured.builder()
                .name(request.getName())
                .cpf(request.getCpf())
                .email(request.getEmail())
                .phone(request.getPhone())
                .createdAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build();

        insured = insuredRepository.save(insured);

        return entityToDto(insured);
    }

    public Insured findByCPF(String cpf, Locale locale) {
        return insuredRepository.findByCpf(cpf)
                .orElseThrow(() -> new NotFoundException(messageSource.getMessage(UserConstants.USER_NOT_FOUND, new Object[] {cpf}, locale)));
    }

    private void validateUserByCPF(String cpf, Locale locale) {
        if (insuredRepository.existsByCpf(cpf))
            throw new AlreadyExistsException(
                    messageSource.getMessage(UserConstants.USER_ALREADY_EXISTS, new Object[] { cpf }, locale));
    }

    private InsuredResponseDto entityToDto(Insured insured) {
        return modelMapper.map(insured, InsuredResponseDto.class);
    }
}
