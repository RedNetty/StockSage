package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.dto.mapper.UserMapper;
import com.portfolio.stocksage.dto.request.LoginDTO;
import com.portfolio.stocksage.dto.request.SignupDTO;
import com.portfolio.stocksage.dto.response.JwtDTO;
import com.portfolio.stocksage.dto.response.UserDTO;
import com.portfolio.stocksage.entity.Role;
import com.portfolio.stocksage.entity.User;
import com.portfolio.stocksage.exception.ResourceNotFoundException;
import com.portfolio.stocksage.repository.RoleRepository;
import com.portfolio.stocksage.repository.UserRepository;
import com.portfolio.stocksage.security.JwtTokenProvider;
import com.portfolio.stocksage.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;

    @Override
    public JwtDTO login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + loginDTO.getUsername()));

        UserDTO userDTO = userMapper.toDto(user);

        return JwtDTO.builder()
                .tokenType("Bearer")
                .accessToken(jwt)
                .expiresIn(86400000L) // 24 hours in milliseconds
                .user(userDTO)
                .build();
    }

    @Override
    @Transactional
    public UserDTO signup(SignupDTO signupDTO) {
        // Check if username is already taken
        if (userRepository.existsByUsername(signupDTO.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Check if email is already in use
        if (userRepository.existsByEmail(signupDTO.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // Create new user
        User user = new User();
        user.setUsername(signupDTO.getUsername());
        user.setEmail(signupDTO.getEmail());
        user.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        user.setFirstName(signupDTO.getFirstName());
        user.setLastName(signupDTO.getLastName());
        user.setActive(true);

        // Assign roles
        Set<Role> roles = new HashSet<>();

        if (signupDTO.getRoles() == null || signupDTO.getRoles().isEmpty()) {
            // Default role: USER
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role 'USER' not found"));
            roles.add(userRole);
        } else {
            signupDTO.getRoles().forEach(roleName -> {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
                roles.add(role);
            });
        }

        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }
}