package com.portfolio.stocksage.controller.web;

import com.portfolio.stocksage.dto.request.UserCreateDTO;
import com.portfolio.stocksage.dto.request.UserUpdateDTO;
import com.portfolio.stocksage.dto.response.RoleDTO;
import com.portfolio.stocksage.dto.response.UserDTO;
import com.portfolio.stocksage.security.SecurityUtils;
import com.portfolio.stocksage.service.RoleService;
import com.portfolio.stocksage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class WebUserController {

    private final UserService userService;
    private final RoleService roleService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String getAllUsers(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sort,
            @RequestParam(defaultValue = "asc") String dir) {

        // Create pageable request
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sort));

        // Get users
        Page<UserDTO> users = userService.getAllUsers(pageRequest);

        // Add attributes to model
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("totalItems", users.getTotalElements());
        model.addAttribute("sortField", sort);
        model.addAttribute("sortDir", dir);
        model.addAttribute("reverseSortDir", "asc".equals(dir) ? "desc" : "asc");

        return "user/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwnerOrAdmin(#id)")
    public String getUserDetails(@PathVariable Long id, Model model) {
        UserDTO user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "user/details";
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new UserCreateDTO());
        model.addAttribute("allRoles", roleService.getAllRoles());
        return "user/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(
            @Valid @ModelAttribute("user") UserCreateDTO user,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("allRoles", roleService.getAllRoles());
            return "user/create";
        }

        // Check if username is unique
        if (!userService.isUsernameUnique(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username must be unique");
            model.addAttribute("allRoles", roleService.getAllRoles());
            return "user/create";
        }

        // Check if email is unique
        if (!userService.isEmailUnique(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email must be unique");
            model.addAttribute("allRoles", roleService.getAllRoles());
            return "user/create";
        }

        // Create user
        UserDTO savedUser = userService.createUser(user);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "User created successfully");

        return "redirect:/users/" + savedUser.getId();
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwnerOrAdmin(#id)")
    public String showEditForm(@PathVariable Long id, Model model) {
        UserDTO user = userService.getUserById(id);

        // Convert to UserUpdateDTO for the form
        UserUpdateDTO userForm = new UserUpdateDTO();
        userForm.setUsername(user.getUsername());
        userForm.setEmail(user.getEmail());
        userForm.setFirstName(user.getFirstName());
        userForm.setLastName(user.getLastName());
        userForm.setActive(user.isActive());

        // Set role IDs
        if (user.getRoles() != null) {
            userForm.setRoleIds(user.getRoles().stream()
                    .map(RoleDTO::getId)
                    .collect(Collectors.toSet()));
        }

        model.addAttribute("user", userForm);
        model.addAttribute("userId", id);
        model.addAttribute("allRoles", roleService.getAllRoles());
        model.addAttribute("isAdmin", securityUtils.isCurrentUserAdmin());

        return "user/edit";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityUtils.isOwnerOrAdmin(#id)")
    public String updateUser(
            @PathVariable Long id,
            @Valid @ModelAttribute("user") UserUpdateDTO user,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("allRoles", roleService.getAllRoles());
            model.addAttribute("isAdmin", securityUtils.isCurrentUserAdmin());
            return "user/edit";
        }

        // Get existing user to check for username/email changes
        UserDTO existingUser = userService.getUserById(id);

        // Check if username is being changed and if it's unique
        if (!existingUser.getUsername().equals(user.getUsername()) &&
                !userService.isUsernameUnique(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username must be unique");
            model.addAttribute("userId", id);
            model.addAttribute("allRoles", roleService.getAllRoles());
            model.addAttribute("isAdmin", securityUtils.isCurrentUserAdmin());
            return "user/edit";
        }

        // Check if email is being changed and if it's unique
        if (!existingUser.getEmail().equals(user.getEmail()) &&
                !userService.isEmailUnique(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email must be unique");
            model.addAttribute("userId", id);
            model.addAttribute("allRoles", roleService.getAllRoles());
            model.addAttribute("isAdmin", securityUtils.isCurrentUserAdmin());
            return "user/edit";
        }

        // Non-admin users can't change roles
        if (!securityUtils.isCurrentUserAdmin()) {
            // Reset role IDs to existing ones
            user.setRoleIds(existingUser.getRoles().stream()
                    .map(RoleDTO::getId)
                    .collect(Collectors.toSet()));
        }

        // Update user
        UserDTO updatedUser = userService.updateUser(id, user);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");

        return "redirect:/users/" + updatedUser.getId();
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        Long currentUserId = securityUtils.getCurrentUserId();
        model.addAttribute("userId", currentUserId);
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam Long userId,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        // Check if user is authorized to change this password
        if (!securityUtils.isCurrentUserAdmin() && !securityUtils.getCurrentUserId().equals(userId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to change this password");
            return "redirect:/users/change-password";
        }

        // Check if new password matches confirmation
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password and confirmation do not match");
            return "redirect:/users/change-password";
        }

        try {
            userService.changePassword(userId, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully");
            return "redirect:/users/" + userId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/change-password";
        }
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showDeleteConfirmation(@PathVariable Long id, Model model) {
        UserDTO user = userService.getUserById(id);
        model.addAttribute("user", user);

        // Check if this is the current user
        Long currentUserId = securityUtils.getCurrentUserId();
        model.addAttribute("isSelf", id.equals(currentUserId));

        return "user/delete";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        // Prevent self-deletion
        Long currentUserId = securityUtils.getCurrentUserId();
        if (id.equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot delete your own account");
            return "redirect:/users";
        }

        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/users";
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public String deactivateUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        // Prevent self-deactivation
        Long currentUserId = securityUtils.getCurrentUserId();
        if (id.equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot deactivate your own account");
            return "redirect:/users/" + id;
        }

        userService.deactivateUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User deactivated successfully");

        return "redirect:/users/" + id;
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public String activateUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        userService.activateUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User activated successfully");

        return "redirect:/users/" + id;
    }

    @GetMapping("/profile")
    public String showUserProfile(Model model) {
        Long currentUserId = securityUtils.getCurrentUserId();
        UserDTO user = userService.getUserById(currentUserId);
        model.addAttribute("user", user);
        return "user/profile";
    }

    @GetMapping("/check-username")
    @ResponseBody
    public boolean checkUsernameUnique(@RequestParam String username, @RequestParam(required = false) Long id) {
        // If ID is provided, we're checking for an update operation
        if (id != null) {
            UserDTO existing = userService.getUserById(id);
            // If the username hasn't changed, it's valid
            if (existing.getUsername().equals(username)) {
                return true;
            }
        }
        return userService.isUsernameUnique(username);
    }

    @GetMapping("/check-email")
    @ResponseBody
    public boolean checkEmailUnique(@RequestParam String email, @RequestParam(required = false) Long id) {
        // If ID is provided, we're checking for an update operation
        if (id != null) {
            UserDTO existing = userService.getUserById(id);
            // If the email hasn't changed, it's valid
            if (existing.getEmail().equals(email)) {
                return true;
            }
        }
        return userService.isEmailUnique(email);
    }
}