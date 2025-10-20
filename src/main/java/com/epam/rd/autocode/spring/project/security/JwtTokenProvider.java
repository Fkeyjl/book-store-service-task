package com.epam.rd.autocode.spring.project.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpirationInMs;

    public String generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        ClaimsBuilder claims = Jwts.claims().subject(authentication.getName()).id(user.getId().toString());
        claims.add("role", authorities);
        claims.add("type", "access");
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        return Jwts.builder()
                .claims(claims.build())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        ClaimsBuilder claims = Jwts.claims().subject(authentication.getName()).id(user.getId().toString());
        claims.add("role", authorities);
        claims.add("type", "refresh");
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationInMs);
        return Jwts.builder()
                .claims(claims.build())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)), Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = (String) claims.get("type");
            if (!"refresh".equals(tokenType)) {
                log.warn("Token is not a refresh token");
                return false;
            }
            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                log.warn("Refresh token has expired");
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Refresh token has expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    @Deprecated
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String tokenType = (String) claims.get("type");
            if (!"access".equals(tokenType)) {
                log.warn("Token is not an access token");
                return false;
            }
            Date expiration = claims.getExpiration();
            if (expiration == null || expiration.before(new Date())) {
                log.warn("Access token has expired");
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Access token has expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("Cannot extract username from expired token");
            throw new IllegalArgumentException("Token has expired", e);
        } catch (Exception e) {
            log.error("Cannot extract username from token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token", e);
        }
    }
}
