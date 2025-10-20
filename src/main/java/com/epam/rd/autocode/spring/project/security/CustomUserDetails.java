package com.epam.rd.autocode.spring.project.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final LocalDateTime lockTime;
    private final boolean blocked;

    public CustomUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities, LocalDateTime lockTime, boolean blocked) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.lockTime = lockTime;
        this.blocked = blocked;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (lockTime == null && !blocked) {
            return true;
        }
        if (lockTime.isBefore(LocalDateTime.now())) {
            return true;
        }
        return !blocked;
    }
}
