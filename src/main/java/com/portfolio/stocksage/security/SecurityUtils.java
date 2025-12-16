package com.portfolio.stocksage.security;

import com.portfolio.stocksage.entity.User;
import com.portfolio.stocksage.exception.UnauthorizedException;
import com.portfolio.stocksage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Get the username of the current authenticated user
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if (authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }

        return authentication.getPrincipal().toString();
    }

    /**
     * Get the current authenticated user
     */
    public User getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("No authenticated user found");
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found: " + username));
    }

    /**
     * Get the ID of the current authenticated user
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Check if the current user has admin role
     */
    public boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Check if the current user has the specified role
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Check if the current user is the owner of the resource or has admin role
     */
    public boolean isOwnerOrAdmin(Long resourceOwnerId) {
        if (isCurrentUserAdmin()) {
            return true;
        }

        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(resourceOwnerId);
    }
}