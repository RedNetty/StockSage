package com.portfolio.stocksage.service;

import com.portfolio.stocksage.dto.request.PasswordResetDTO;
import com.portfolio.stocksage.dto.request.UserCreateDTO;
import com.portfolio.stocksage.dto.request.UserUpdateDTO;
import com.portfolio.stocksage.dto.response.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    /**
     * Get a user by ID
     */
    UserDTO getUserById(Long id);

    /**
     * Get a user by username
     */
    UserDTO getUserByUsername(String username);

    /**
     * Get a user by email
     */
    UserDTO getUserByEmail(String email);

    /**
     * Get all users with pagination
     */
    Page<UserDTO> getAllUsers(Pageable pageable);

    /**
     * Get users with a specific role
     */
    List<UserDTO> getUsersByRole(String roleName);

    /**
     * Create a new user
     */
    UserDTO createUser(UserCreateDTO userCreateDTO);

    /**
     * Update an existing user
     */
    UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO);

    /**
     * Delete a user
     */
    void deleteUser(Long id);

    /**
     * Change a user's password
     */
    void changePassword(Long id, String currentPassword, String newPassword);

    /**
     * Initiate the password reset process
     * Returns a reset token that should be sent to the user's email
     */
    String initiatePasswordReset(String email);

    /**
     * Reset a user's password using a reset token
     */
    void resetPassword(PasswordResetDTO passwordResetDTO);

    /**
     * Deactivate a user account
     */
    void deactivateUser(Long id);

    /**
     * Activate a user account
     */
    void activateUser(Long id);

    /**
     * Check if a username is unique
     */
    boolean isUsernameUnique(String username);

    /**
     * Check if an email is unique
     */
    boolean isEmailUnique(String email);
}