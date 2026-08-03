package com.santander.msauthservices.services;

import com.santander.msauthservices.constants.UserConstants;
import com.santander.msauthservices.dto.InsuredResponseDto;
import com.santander.msauthservices.dto.UserRequestDto;
import com.santander.msauthservices.exception.AlreadyExistsException;
import com.santander.msauthservices.exception.NotFoundException;
import com.santander.msauthservices.model.Insured;
import com.santander.msauthservices.repository.InsuredRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários da classe {@link InsuredServices}.
 */
@ExtendWith(MockitoExtension.class)
class InsuredServicesTest {

    @Mock
    private InsuredRepository insuredRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private InsuredServices insuredServices;

    private final Locale locale = Locale.getDefault();

    @Test
    void newInsured_deveCriarSeguradoComSucesso_quandoCpfNaoExiste() {
        UserRequestDto request = UserRequestDto.builder()
                .name("Fulano")
                .cpf("12345678900")
                .email("fulano@teste.com")
                .phone("11999999999")
                .build();

        Insured savedInsured = Insured.builder()
                .name(request.getName())
                .cpf(request.getCpf())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        InsuredResponseDto expectedResponse = InsuredResponseDto.builder()
                .name(request.getName())
                .cpf(request.getCpf())
                .email(request.getEmail())
                .build();

        when(insuredRepository.existsByCpf(request.getCpf())).thenReturn(false);
        when(insuredRepository.save(any(Insured.class))).thenReturn(savedInsured);
        when(modelMapper.map(savedInsured, InsuredResponseDto.class)).thenReturn(expectedResponse);

        InsuredResponseDto response = insuredServices.newInsured(request, locale);

        assertThat(response).isEqualTo(expectedResponse);
        verify(insuredRepository, times(1)).save(any(Insured.class));
    }

    @Test
    void newInsured_deveLancarAlreadyExistsException_quandoCpfJaExiste() {
        UserRequestDto request = UserRequestDto.builder()
                .cpf("12345678900")
                .build();

        when(insuredRepository.existsByCpf(request.getCpf())).thenReturn(true);
        when(messageSource.getMessage(eq(UserConstants.USER_ALREADY_EXISTS), any(), eq(locale)))
                .thenReturn("Segurado já existe");

        assertThatThrownBy(() -> insuredServices.newInsured(request, locale))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("Segurado já existe");

        verify(insuredRepository, never()).save(any());
    }

    @Test
    void findByCPF_deveRetornarSegurado_quandoCpfExistir() {
        String cpf = "12345678900";
        Insured insured = Insured.builder().cpf(cpf).build();

        when(insuredRepository.findByCpf(cpf)).thenReturn(Optional.of(insured));

        Insured result = insuredServices.findByCPF(cpf, locale);

        assertThat(result).isEqualTo(insured);
    }

    @Test
    void findByCPF_deveLancarNotFoundException_quandoCpfNaoExistir() {
        String cpf = "00000000000";

        when(insuredRepository.findByCpf(cpf)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(UserConstants.USER_NOT_FOUND), any(), eq(locale)))
                .thenReturn("Segurado não encontrado");

        assertThatThrownBy(() -> insuredServices.findByCPF(cpf, locale))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Segurado não encontrado");
    }

    @Test
    void findByInsureLogged_deveRetornarSegurado_quandoEmailExistir() {
        String email = "fulano@teste.com";
        Insured insured = Insured.builder().email(email).build();

        when(insuredRepository.findByEmail(email)).thenReturn(Optional.of(insured));

        Insured result = insuredServices.findByInsureLogged(email, locale);

        assertThat(result).isEqualTo(insured);
    }

    @Test
    void findByInsureLogged_deveLancarNotFoundException_quandoEmailNaoExistir() {
        String email = "inexistente@teste.com";

        when(insuredRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(UserConstants.USER_NOT_FOUND), any(), eq(locale)))
                .thenReturn("Segurado não encontrado");

        assertThatThrownBy(() -> insuredServices.findByInsureLogged(email, locale))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Segurado não encontrado");
    }
}