package com.santander.msauthservices.services;

import com.santander.msauthservices.constants.UserConstants;
import com.santander.msauthservices.dto.InsuredResponseDto;
import com.santander.msauthservices.dto.UserRequestDto;
import com.santander.msauthservices.dto.UserResponseDto;
import com.santander.msauthservices.exception.AlreadyExistsException;
import com.santander.msauthservices.exception.BadRequestException;
import com.santander.msauthservices.exception.BusinessException;
import com.santander.msauthservices.exception.NotFoundException;
import com.santander.msauthservices.model.Insured;
import com.santander.msauthservices.model.Role;
import com.santander.msauthservices.model.User;
import com.santander.msauthservices.repository.UserRepository;
import com.santander.msauthservices.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
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

    @Test
    void newUser_deveCriarUsuarioComSucesso_quandoRoleNaoForInsured() {
        UserRequestDto request = UserRequestDto.builder()
                .name("Fulano")
                .email("fulano@teste.com")
                .password("123456")
                .role(Role.ADMIN)
                .build();

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

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(modelMapper.map(savedUser, UserResponseDto.class)).thenReturn(expectedResponse);

        UserResponseDto response = userServices.newUser(request, locale);

        assertThat(response).isEqualTo(expectedResponse);
        verify(insuredServices, never()).newInsured(any(), any());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void newUser_deveLancarAlreadyExistsException_quandoEmailJaExiste() {
        UserRequestDto request = UserRequestDto.builder()
                .email("existente@teste.com")
                .role(Role.ADMIN)
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
        when(messageSource.getMessage(eq(UserConstants.USER_ALREADY_EXISTS), any(), eq(locale)))
                .thenReturn("Usuário já existe");

        assertThatThrownBy(() -> userServices.newUser(request, locale))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("Usuário já existe");

        verify(userRepository, never()).save(any());
    }

    @Test
    void newUser_deveCriarInsured_quandoRoleForInsuredECpfPreenchido() {
        UserRequestDto request = UserRequestDto.builder()
                .email("segurado@teste.com")
                .role(Role.INSURED)
                .cpf("12345678900")
                .build();

        Insured createdInsured = Insured.builder().id(10L).build();
        User savedUser = User.builder()
                .email(request.getEmail())
                .role(Role.INSURED)
                .insured(createdInsured)
                .build();
        UserResponseDto expectedResponse = UserResponseDto.builder()
                .email(request.getEmail())
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(insuredServices.newInsured(request, locale)).thenReturn(new InsuredResponseDto());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(modelMapper.map(savedUser, UserResponseDto.class)).thenReturn(expectedResponse);

        UserResponseDto response = userServices.newUser(request, locale);

        assertThat(response).isEqualTo(expectedResponse);
        verify(insuredServices, times(1)).newInsured(request, locale);
    }

    @Test
    void newUser_deveLancarBadRequestException_quandoRoleForInsuredSemCpf() {
        UserRequestDto request = UserRequestDto.builder()
                .email("segurado@teste.com")
                .role(Role.INSURED)
                .cpf(null)
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);

        assertThatThrownBy(() -> userServices.newUser(request, locale))
                .isInstanceOf(BadRequestException.class);

        verify(insuredServices, never()).newInsured(any(), any());
        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // findByEmail / findByEmailDto
    // ---------------------------------------------------------------

    @Test
    void findByEmail_deveRetornarUsuario_quandoExistir() {
        String email = "fulano@teste.com";
        User user = User.builder().email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userServices.findByEmail(email, locale);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findByEmail_deveLancarNotFoundException_quandoNaoExistir() {
        String email = "inexistente@teste.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(UserConstants.USER_NOT_FOUND), any(), eq(locale)))
                .thenReturn("Usuário não encontrado");

        assertThatThrownBy(() -> userServices.findByEmail(email, locale))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }

    @Test
    void findByEmailDto_deveRetornarDtoConvertido_quandoUsuarioExistir() {
        String email = "fulano@teste.com";
        User user = User.builder().email(email).build();
        UserResponseDto expectedDto = UserResponseDto.builder().email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserResponseDto.class)).thenReturn(expectedDto);

        UserResponseDto result = userServices.findByEmailDto(email, locale);

        assertThat(result).isEqualTo(expectedDto);
    }

    // ---------------------------------------------------------------
    // loadUserByUsername (UserDetailsService)
    // ---------------------------------------------------------------

    @Test
    void loadUserByUsername_deveRetornarUserDetails_quandoUsuarioExistir() {
        String email = "fulano@teste.com";
        User user = User.builder().email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails result = userServices.loadUserByUsername(email);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void loadUserByUsername_devePropagarExcecao_quandoUsuarioNaoExistir() {
        String email = "inexistente@teste.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(UserConstants.USER_NOT_FOUND), any(), any()))
                .thenReturn("Usuário não encontrado");

        assertThatThrownBy(() -> userServices.loadUserByUsername(email))
                .isInstanceOf(NotFoundException.class);
    }

    // ---------------------------------------------------------------
    // userLogged (depende do RequestContextHolder / JWT)
    // ---------------------------------------------------------------

    @Test
    void userLogged_deveRetornarEmailDoUsuarioLogado_independenteDaRole() {
        String email = "segurado@teste.com";
        String token = "abc.def.ghi";
        User loggedUser = User.builder().email(email).role(Role.INSURED).build();

        HttpServletRequest httpRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
        ServletRequestAttributes attributes = org.mockito.Mockito.mock(ServletRequestAttributes.class);

        when(httpRequest.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(attributes.getRequest()).thenReturn(httpRequest);
        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(loggedUser));

        try (MockedStatic<RequestContextHolder> mockedHolder = mockStatic(RequestContextHolder.class)) {
            mockedHolder.when(RequestContextHolder::currentRequestAttributes).thenReturn(attributes);

            String result = userServices.userLogged(locale);

            assertThat(result).isEqualTo(email);
        }
    }

    @Test
    void userLogged_devePropagarNotFoundException_quandoUsuarioDoTokenNaoExistir() {
        String email = "inexistente@teste.com";
        String token = "abc.def.ghi";

        HttpServletRequest httpRequest = org.mockito.Mockito.mock(HttpServletRequest.class);
        ServletRequestAttributes attributes = org.mockito.Mockito.mock(ServletRequestAttributes.class);

        when(httpRequest.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(attributes.getRequest()).thenReturn(httpRequest);
        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(UserConstants.USER_NOT_FOUND), any(), eq(locale)))
                .thenReturn("Usuário não encontrado");

        try (MockedStatic<RequestContextHolder> mockedHolder = mockStatic(RequestContextHolder.class)) {
            mockedHolder.when(RequestContextHolder::currentRequestAttributes).thenReturn(attributes);

            assertThatThrownBy(() -> userServices.userLogged(locale))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}