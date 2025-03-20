package com.portfolio.stocksage.service;

import com.portfolio.stocksage.dto.response.RoleDTO;

import java.util.List;

public interface RoleService {

    /**
     * Get all roles in the system
     */
    List<RoleDTO> getAllRoles();

    /**
     * Get a role by ID
     */
    RoleDTO getRoleById(Long id);

    /**
     * Get a role by name
     */
    RoleDTO getRoleByName(String name);

    /**
     * Create a new role
     */
    RoleDTO createRole(String name);

    /**
     * Delete a role
     */
    void deleteRole(Long id);

    /**
     * Check if a role name is unique
     */
    boolean isRoleNameUnique(String name);

    /**
     * Get count of users with a specific role
     */
    int getUserCountByRole(Long roleId);
}