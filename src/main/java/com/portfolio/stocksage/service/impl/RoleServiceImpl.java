package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.dto.response.RoleDTO;
import com.portfolio.stocksage.entity.Role;
import com.portfolio.stocksage.exception.BadRequestException;
import com.portfolio.stocksage.exception.ResourceNotFoundException;
import com.portfolio.stocksage.repository.RoleRepository;
import com.portfolio.stocksage.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return mapToDto(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
        return mapToDto(role);
    }

    @Override
    @Transactional
    public RoleDTO createRole(String name) {
        // Check if role with this name already exists
        if (roleRepository.existsByName(name)) {
            throw new BadRequestException("Role with name '" + name + "' already exists");
        }

        // Create and save the new role
        Role role = new Role();
        role.setName(name);
        Role savedRole = roleRepository.save(role);

        return mapToDto(savedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        // Check if role has users
        if (!role.getUsers().isEmpty()) {
            throw new BadRequestException("Cannot delete role that has associated users. Role has " +
                    role.getUsers().size() + " users.");
        }

        roleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoleNameUnique(String name) {
        return !roleRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public int getUserCountByRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        return role.getUsers().size();
    }

    /**
     * Maps a Role entity to a RoleDTO
     */
    private RoleDTO mapToDto(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .build();
    }
}