package com.santander.msauthservices.config;
import com.santander.msauthservices.dto.UserResponseDto;
import com.santander.msauthservices.model.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper mapper() {
        return new ModelMapper();
    }

}