package com.portfolio.stocksage.controller.web;

import com.portfolio.stocksage.dto.request.UserUpdateDTO;
import com.portfolio.stocksage.dto.response.NotificationDTO;
import com.portfolio.stocksage.dto.response.RoleDTO;
import com.portfolio.stocksage.dto.response.UserDTO;
import com.portfolio.stocksage.security.SecurityUtils;
import com.portfolio.stocksage.service.NotificationService;
import com.portfolio.stocksage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class WebProfileController {

    private final UserService userService;
    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String viewProfile(Model model) {
        Long currentUserId = securityUtils.getCurrentUserId();
        UserDTO user = userService.getUserById(currentUserId);
        model.addAttribute("user", user);

        // Get unread notification count
        long unreadCount = notificationService.countUnreadNotifications(currentUserId);
        model.addAttribute("unreadNotifications", unreadCount);

        return "profile/view";
    }

    @GetMapping("/edit")
    public String showEditProfileForm(Model model) {
        Long currentUserId = securityUtils.getCurrentUserId();
        UserDTO user = userService.getUserById(currentUserId);

        // Convert to UserUpdateDTO for the form
        UserUpdateDTO userForm = new UserUpdateDTO();
        userForm.setUsername(user.getUsername());
        userForm.setEmail(user.getEmail());
        userForm.setFirstName(user.getFirstName());
        userForm.setLastName(user.getLastName());
        userForm.setActive(user.isActive());

        // Set role IDs (but these won't be editable by the user)
        if (user.getRoles() != null) {
            userForm.setRoleIds(user.getRoles().stream()
                    .map(RoleDTO::getId)
                    .collect(Collectors.toSet()));
        }

        model.addAttribute("user", userForm);
        model.addAttribute("userId", currentUserId);

        return "profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(
            @Valid @ModelAttribute("user") UserUpdateDTO user,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long currentUserId = securityUtils.getCurrentUserId();

        // Check for validation errors
        if (result.hasErrors()) {
            model.addAttribute("userId", currentUserId);
            return "profile/edit";
        }

        // Get existing user to check for username/email changes
        UserDTO existingUser = userService.getUserById(currentUserId);

        // Check if username is being changed and if it's unique
        if (!existingUser.getUsername().equals(user.getUsername()) &&
                !userService.isUsernameUnique(user.getUsername())) {
            result.rejectValue("username", "error.user", "Username must be unique");
            model.addAttribute("userId", currentUserId);
            return "profile/edit";
        }

        // Check if email is being changed and if it's unique
        if (!existingUser.getEmail().equals(user.getEmail()) &&
                !userService.isEmailUnique(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email must be unique");
            model.addAttribute("userId", currentUserId);
            return "profile/edit";
        }

        // Keep the existing roles
        user.setRoleIds(existingUser.getRoles().stream()
                .map(RoleDTO::getId)
                .collect(Collectors.toSet()));

        // Update user
        UserDTO updatedUser = userService.updateUser(currentUserId, user);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");

        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "profile/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        Long currentUserId = securityUtils.getCurrentUserId();

        // Check if new password matches confirmation
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password and confirmation do not match");
            return "redirect:/profile/change-password";
        }

        try {
            userService.changePassword(currentUserId, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile/change-password";
        }
    }

    @GetMapping("/notifications")
    public String viewNotifications(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {

        Long currentUserId = securityUtils.getCurrentUserId();

        // Create pageable request
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Get notifications
        Page<NotificationDTO> notifications;
        if (unreadOnly) {
            notifications = notificationService.getUnreadUserNotifications(currentUserId, pageRequest);
        } else {
            notifications = notificationService.getUserNotifications(currentUserId, pageRequest);
        }

        // Add attributes to model
        model.addAttribute("notifications", notifications);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notifications.getTotalPages());
        model.addAttribute("totalItems", notifications.getTotalElements());
        model.addAttribute("unreadOnly", unreadOnly);

        return "profile/notifications";
    }

    @PostMapping("/notifications/{id}/mark-read")
    @ResponseBody
    public boolean markNotificationAsRead(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsAsRead(RedirectAttributes redirectAttributes) {
        Long currentUserId = securityUtils.getCurrentUserId();
        notificationService.markAllAsRead(currentUserId);
        redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read");
        return "redirect:/profile/notifications";
    }
}