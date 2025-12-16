package com.portfolio.stocksage.security;

import com.portfolio.stocksage.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom implementation of Spring Security's UserDetails
 * Adapts our User entity to the UserDetails interface expected by Spring Security
 */
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final User user;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.user = user;

        // Convert roles to authorities
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // We don't track account expiration
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // We don't track account locking
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // We don't track credential expiration
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    /**
     * Get the underlying user
     */
    public User getUser() {
        return user;
    }

    /**
     * Get user's ID
     */
    public Long getId() {
        return user.getId();
    }

    /**
     * Get user's email
     */
    public String getEmail() {
        return user.getEmail();
    }

    /**
     * Get user's full name
     */
    public String getFullName() {
        return user.getFirstName() + " " + user.getLastName();
    }

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String roleName) {
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + roleName));
    }
}