package com.epam.rd.autocode.spring.project.service;

public interface AuthenticationService {
    void blockUser(Long userId);
    void unblockUser(Long userId);
    void increaseFailedLoginAttempts(String email);
    void resetFailedLoginAttempts(String email);
    boolean isUserLocked(String email);
}
