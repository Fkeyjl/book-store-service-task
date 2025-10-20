package com.epam.rd.autocode.spring.project.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomJwtValidator implements OAuth2TokenValidator<Jwt> {

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String tokenType = jwt.getClaimAsString("type");
        
        if (tokenType == null) {
            log.warn("JWT token does not have 'type' claim");
            OAuth2Error error = new OAuth2Error("invalid_token", "Token missing 'type' claim", null);
            return OAuth2TokenValidatorResult.failure(error);
        }
        
        if (!"access".equals(tokenType)) {
            log.warn("JWT token is not an access token. Type: {}", tokenType);
            OAuth2Error error = new OAuth2Error("invalid_token", "Token is not an access token", null);
            return OAuth2TokenValidatorResult.failure(error);
        }
        
        log.debug("JWT token validated successfully. Type: {}", tokenType);
        return OAuth2TokenValidatorResult.success();
    }
}
