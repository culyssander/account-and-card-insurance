package com.santander.msauthservices.services;

import com.santander.msauthservices.dto.UserRequestDto;
import com.santander.msauthservices.dto.UserResponseDto;
import com.santander.msauthservices.model.Role;
import com.santander.msauthservices.model.User;
import com.santander.msauthservices.repository.UserRepository;
import com.santander.msauthservices.util.JWTUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServicesTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private InsuredServices insuredServices;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private JWTUtil jwtUtil;

    @InjectMocks
    private UserServices userServices;

    private final Locale locale = Locale.getDefault();
    private final String NOME = "Fulano";
    private final String EMAIL = "fulano@teste.com";
    private final String PASSWORD = "123456";

    @Test
    void newUserDeveSerCriandoComSucessoQuandoNaoForInsured() {
        UserRequestDto request = getUserRequestDto();

        User savedUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .role(Role.ADMIN)
                .active(true)
                .build();

        UserResponseDto expectedResponse = UserResponseDto.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(modelMapper.map(savedUser, UserResponseDto.class)).thenReturn(expectedResponse);

        UserResponseDto response = userServices.newUser(request, locale);

        assertThat(response).isEqualTo(expectedResponse);
        verify(insuredServices, never()).newInsured(any(), any());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void newUser() {
    }

    @Test
    void findByEmailDto() {
    }

    @Test
    void findByEmail() {
    }

    @Test
    void loadUserByUsername() {
    }

    @Test
    void userLogged() {
    }

    private UserRequestDto getUserRequestDto() {
        return UserRequestDto.builder()
                .name(NOME)
                .email(EMAIL)
                .password(PASSWORD)
                .role(Role.ADMIN)
                .build();
    }

    private User getUser() {
        return User.builder()
                .name(NOME)
                .email(EMAIL)
                .password(PASSWORD)
                .role(Role.ADMIN)
                .build();
    }
}