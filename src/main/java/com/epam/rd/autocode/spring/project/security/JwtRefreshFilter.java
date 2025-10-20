package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.service.AuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRefreshFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final AuthenticationService authenticationService;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpirationInMs;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = extractTokenFromCookie(request, "JWT_TOKEN");
        String refreshToken = extractTokenFromCookie(request, "REFRESH_TOKEN");

        if (jwtToken != null && isValidAccessToken(jwtToken)) {
            log.debug("JWT_TOKEN is valid, continuing request");
            filterChain.doFilter(request, response);
            return;
        }

        if (refreshToken != null) {
            log.debug("JWT_TOKEN is invalid/expired, attempting refresh with REFRESH_TOKEN");
            
            if (attemptTokenRefresh(refreshToken, request, response)) {
                log.info("Token refresh successful, continuing request");
                filterChain.doFilter(request, response);
                return;
            } else {
                log.warn("Token refresh failed");
            }
        }

        log.debug("No valid tokens, passing to Spring Security");
        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String uri) {
        return uri.startsWith("/css/") ||
               uri.startsWith("/js/") ||
               uri.startsWith("/images/") ||
               uri.startsWith("/img/") ||
               uri.equals("/favicon.ico") ||
               uri.equals("/error") ||
               uri.startsWith("/h2-console/") ||
               uri.equals("/register") ||
               uri.equals("/login") ||
               uri.equals("/") ||
               uri.equals("/api/auth/refresh") ||
               uri.startsWith("/categories") ||
               uri.startsWith("/books") ||
               uri.startsWith("/cart/");
    }


    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isValidAccessToken(String token) {
        try {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            return username != null;
        } catch (Exception e) {
            log.debug("JWT_TOKEN validation failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean attemptTokenRefresh(String refreshToken, 
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        try {
            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                log.warn("Invalid or expired refresh token");
                return false;
            }

            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

            if (authenticationService.isUserLocked(username)) {
                log.warn("User account is locked: {}", username);
                return false;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!userDetails.isAccountNonLocked() || !userDetails.isEnabled()) {
                log.warn("User account is locked or disabled: {}", username);
                return false;
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            String newAccessToken = jwtTokenProvider.generateToken(authentication);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            setTokenCookies(response, newAccessToken, newRefreshToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Tokens refreshed successfully for user: {}", username);
            return true;

        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage());
            return false;
        }
    }

    private void setTokenCookies(HttpServletResponse response, String jwtToken, String refreshToken) {
        Cookie jwtCookie = new Cookie("JWT_TOKEN", jwtToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) (jwtExpirationInMs / 1000));
        response.addCookie(jwtCookie);

        Cookie refreshCookie = new Cookie("REFRESH_TOKEN", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (refreshTokenExpirationInMs / 1000));
        response.addCookie(refreshCookie);
    }
}
