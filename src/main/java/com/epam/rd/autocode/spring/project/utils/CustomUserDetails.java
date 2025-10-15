package com.epam.rd.autocode.spring.project.utils;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean blocked;

    public CustomUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities, boolean blocked) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.blocked = blocked;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !blocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
