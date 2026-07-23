package com.santander.msauthservices.services;

import com.santander.msauthservices.constants.UserConstants;
import com.santander.msauthservices.dto.UserRequestDto;
import com.santander.msauthservices.dto.UserResponseDto;
import com.santander.msauthservices.exception.AlreadyExistsException;
import com.santander.msauthservices.exception.NotFoundException;
import com.santander.msauthservices.model.Insured;
import com.santander.msauthservices.model.Role;
import com.santander.msauthservices.model.User;
import com.santander.msauthservices.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@AllArgsConstructor
public class UserServices implements UserDetailsService {

    private UserRepository userRepository;
    private MessageSource messageSource;
    private InsuredServices insuredServices;
    private ModelMapper modelMapper;

    public UserResponseDto newUser(UserRequestDto request, Locale locale) {
        validateUserByEmail(request.getEmail(), locale);
        BigInteger insureId = null;

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

        return  entityToDto(user);
    }

    public UserResponseDto findByEmailDto(String email, Locale locale) {
        User byEmail = findByEmail(email, locale);
        return entityToDto(byEmail);
    }

    public User findByEmail(String email, Locale locale) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(messageSource.getMessage(UserConstants.USER_NOT_FOUND, new Object[] {}, locale)));
    }

    private void validateUserByEmail(String email, Locale locale) {
        if (userRepository.existsByEmail(email))
            throw new AlreadyExistsException(
                    messageSource.getMessage(UserConstants.USER_ALREADY_EXISTS, new Object[] { email }, locale));
    }

    private BigInteger createNewInsured(UserRequestDto userRequestDto, Locale locale) {
        return insuredServices.newInsured(userRequestDto, locale).getId();
    }

    private UserResponseDto entityToDto(User user) {
        return modelMapper.map(user, UserResponseDto.class);
    }

    private Insured getInsured(BigInteger insuredId) {
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
}
