package com.santander.msauthservices.services;

import com.santander.msauthservices.constants.UserConstants;
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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class UserServices implements UserDetailsService {

    private UserRepository userRepository;
    private MessageSource messageSource;
    private InsuredServices insuredServices;
    private ModelMapper modelMapper;
    private JWTUtil jwtUtil;

    public UserResponseDto newUser(UserRequestDto request, Locale locale) {
        validateUserByEmail(request.getEmail(), locale);
        Long insureId = null;

        if (request.getRole().equals(Role.INSURED)) {
            insureId = createNewInsured(request, locale);
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(hashPassword(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .insured(getInsured(insureId))
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        UserResponseDto dto = entityToDto(user);
        log.info("CREATE NEW User: {} ", dto);
        return dto;
    }

    public UserResponseDto findByEmailDto(String email, Locale locale) {
        User byEmail = findByEmail(email, locale);
        UserResponseDto dto = entityToDto(byEmail);
        log.info("find user {}", dto);
        return dto;
    }

    public User findByEmail(String email, Locale locale) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageSource.getMessage(UserConstants.USER_NOT_FOUND, new Object[] {email}, locale)));
    }

    private void validateUserByEmail(String email, Locale locale) {
        if (userRepository.existsByEmail(email))
            throw new AlreadyExistsException(
                    messageSource.getMessage(UserConstants.USER_ALREADY_EXISTS, new Object[] { email }, locale));
    }

    private Long createNewInsured(UserRequestDto userRequestDto, Locale locale) {
        if (Objects.isNull(userRequestDto.getCpf())) {
            throw new BadRequestException("EMPTY CPF");
        }
        return insuredServices.newInsured(userRequestDto, locale).getId();
    }

    private UserResponseDto entityToDto(User user) {
        return modelMapper.map(user, UserResponseDto.class);
    }

    private Insured getInsured(Long insuredId) {
        if (insuredId != null)
            return Insured.builder().id(insuredId).build();
        return null;
    }

    private String hashPassword(String password) {
        return new BCryptPasswordEncoder().encode(password);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByEmail(username, Locale.ENGLISH);
    }

    private String getLoggedUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization").split(" ")[1];
        return jwtUtil.extractUsername(token);
    }

    private User logged(Locale locale) throws BusinessException {
        String email = getLoggedUser();

        User user = findByEmail(email, locale);

        if (Objects.nonNull(user))
            return user;

        throw new BusinessException("Error with logged-in user");
    }

    public String userLogged(Locale locale) {
        User user = logged(locale);

        return user.getEmail();
    }
}
