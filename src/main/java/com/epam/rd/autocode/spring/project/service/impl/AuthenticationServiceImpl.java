package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.repo.UserRepository;
import com.epam.rd.autocode.spring.project.service.AuthenticationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    
    @Value("${security.max-login-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${security.account-lock-duration-minutes:15}")
    private int accountLockDurationMinutes;

    @Override
    @Transactional
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));
        user.setIsBlocked(true);
        user.setLockTime(null);
        userRepository.save(user);
        log.warn("User permanently blocked by administrator: {} (ID: {})", user.getEmail(), userId);
    }

    @Override
    @Transactional
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found"));
        user.setIsBlocked(false);
        user.setLockTime(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        log.info("User unblocked by administrator: {} (ID: {})", user.getEmail(), userId);
    }

    @Override
    @Transactional
    public void increaseFailedLoginAttempts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));

        unlockIfTimeExpired(user);

        if (user.getIsBlocked()) {
            log.warn("Login attempt for permanently blocked account: {}", email);
            throw new LockedException("Account is permanently blocked by administrator.");
        }

        int currentAttempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
        user.setFailedLoginAttempts(currentAttempts + 1);
        
        log.debug("Failed login attempt for user: {} (attempt {}/{})", email, user.getFailedLoginAttempts(), maxLoginAttempts);

        if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
            user.setIsBlocked(true);
            user.setLockTime(LocalDateTime.now().plusMinutes(accountLockDurationMinutes));
            log.warn("Account temporarily locked due to too many failed attempts: {} (duration: {} minutes)", 
                     email, accountLockDurationMinutes);
        }
        
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void resetFailedLoginAttempts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));

        LocalDateTime lockTimeBeforeReset = user.getLockTime();
        boolean wasBlocked = user.getIsBlocked();

        user.setFailedLoginAttempts(0);

        if (lockTimeBeforeReset != null) {
            user.setLockTime(null);
            user.setIsBlocked(false);
            log.info("Temporary lock removed after successful login: {}", email);
        } else if (!wasBlocked) {
            log.debug("Failed login attempts reset for user: {}", email);
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean isUserLocked(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new EntityNotFoundException("User with username " + username + " not found"));

        if (!user.getIsBlocked()) {
            return false;
        }

        if (user.getLockTime() != null) {
            if (user.getLockTime().isAfter(LocalDateTime.now())) {
                return true;
            } else {
                unlockIfTimeExpired(user);
                return false;
            }
        }
        return true;
    }

    @Transactional
    protected void unlockIfTimeExpired(User user) {
        if (user.getIsBlocked() && user.getLockTime() != null && 
            user.getLockTime().isBefore(LocalDateTime.now())) {
            user.setIsBlocked(false);
            user.setLockTime(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            log.info("Account automatically unlocked after lock time expired: {}", user.getEmail());
        }
    }
}
