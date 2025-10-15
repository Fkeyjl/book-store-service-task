package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.dto.UserRegistrationDTO;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class BaseConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.createTypeMap(UserRegistrationDTO.class, User.class)
                .setPostConverter(context -> {
                    User destination = context.getDestination();
                    if (destination.getRole() == null) {
                        destination.setRole(Role.CUSTOMER);
                    }
                    if (destination.getBalance() == null) {
                        destination.setBalance(new BigDecimal("0.00"));
                    }
                    return destination;
                });
        return modelMapper;
    }
}
