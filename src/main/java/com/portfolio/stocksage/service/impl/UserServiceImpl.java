package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.dto.mapper.UserMapper;
import com.portfolio.stocksage.dto.request.PasswordResetDTO;
import com.portfolio.stocksage.dto.request.UserCreateDTO;
import com.portfolio.stocksage.dto.request.UserUpdateDTO;
import com.portfolio.stocksage.dto.response.UserDTO;
import com.portfolio.stocksage.entity.Role;
import com.portfolio.stocksage.entity.User;
import com.portfolio.stocksage.exception.BadRequestException;
import com.portfolio.stocksage.exception.ResourceNotFoundException;
import com.portfolio.stocksage.repository.RoleRepository;
import com.portfolio.stocksage.repository.UserRepository;
import com.portfolio.stocksage.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByRole(String roleName) {
        return userRepository.findByRoleName(roleName).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        // Check if username is already taken
        if (userRepository.existsByUsername(userCreateDTO.getUsername())) {
            throw new BadRequestException("Username is already taken: " + userCreateDTO.getUsername());
        }

        // Check if email is already in use
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new BadRequestException("Email is already in use: " + userCreateDTO.getEmail());
        }

        User user = userMapper.toEntity(userCreateDTO);

        // Encode password
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));

        // Set default role if none provided
        if (userCreateDTO.getRoleIds() == null || userCreateDTO.getRoleIds().isEmpty()) {
            Role userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role 'USER' not found"));
            user.getRoles().add(userRole);
        } else {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : userCreateDTO.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        // Set active status
        user.setActive(true);

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check if username is being changed and if the new username is already taken
        if (!existingUser.getUsername().equals(userUpdateDTO.getUsername()) &&
                userRepository.existsByUsername(userUpdateDTO.getUsername())) {
            throw new BadRequestException("Username is already taken: " + userUpdateDTO.getUsername());
        }

        // Check if email is being changed and if the new email is already in use
        if (!existingUser.getEmail().equals(userUpdateDTO.getEmail()) &&
                userRepository.existsByEmail(userUpdateDTO.getEmail())) {
            throw new BadRequestException("Email is already in use: " + userUpdateDTO.getEmail());
        }

        // Update user fields
        existingUser.setUsername(userUpdateDTO.getUsername());
        existingUser.setEmail(userUpdateDTO.getEmail());
        existingUser.setFirstName(userUpdateDTO.getFirstName());
        existingUser.setLastName(userUpdateDTO.getLastName());
        existingUser.setActive(userUpdateDTO.isActive());

        // Update roles if provided
        if (userUpdateDTO.getRoleIds() != null && !userUpdateDTO.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : userUpdateDTO.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
                roles.add(role);
            }
            existingUser.setRoles(roles);
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update with new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Generate a unique reset token
        String resetToken = UUID.randomUUID().toString();

        // In a real implementation, you would store this token in a database table
        // with an expiration time, along with the user ID it's associated with
        // For this example, we'll just log it
        log.info("Password reset token for user {}: {}", user.getUsername(), resetToken);

        return resetToken;
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetDTO passwordResetDTO) {
        // In a real implementation, you would validate the token from the database
        // and check if it's still valid

        // For this example, we'll assume the token is valid and find the user by email
        User user = userRepository.findByEmail(passwordResetDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + passwordResetDTO.getEmail()));

        // Update the password
        user.setPassword(passwordEncoder.encode(passwordResetDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameUnique(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailUnique(String email) {
        return !userRepository.existsByEmail(email);
    }
}