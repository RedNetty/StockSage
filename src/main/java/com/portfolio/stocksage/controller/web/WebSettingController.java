package com.portfolio.stocksage.controller.web;

import com.portfolio.stocksage.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class WebSettingController {

    private final SettingService settingService;

    @GetMapping
    public String getAllSettings(Model model) {
        Map<String, String> settings = settingService.getAllSettings();
        model.addAttribute("settings", settings);
        return "settings/list";
    }

    @GetMapping("/edit")
    public String showEditForm(Model model) {
        Map<String, String> settings = settingService.getAllSettings();
        model.addAttribute("settings", settings);
        model.addAttribute("lowStockThreshold", settingService.getLowStockThreshold());
        model.addAttribute("notificationsEnabled", settingService.isNotificationsEnabled());
        model.addAttribute("emailNotificationsEnabled", settingService.isEmailNotificationsEnabled());
        return "settings/edit";
    }

    @PostMapping("/update")
    public String updateSettings(
            @RequestParam Map<String, String> formData,
            RedirectAttributes redirectAttributes) {

        Map<String, String> settingsToUpdate = new HashMap<>();

        // Extract the actual settings from form data (exclude CSRF token and other form fields)
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (entry.getKey().startsWith("setting_")) {
                String key = entry.getKey().substring(8); // Remove "setting_" prefix
                settingsToUpdate.put(key, entry.getValue());
            }
        }

        // Checkbox handling (will only be present if checked)
        boolean notificationsEnabled = formData.containsKey("notifications_enabled");
        boolean emailNotificationsEnabled = formData.containsKey("email_notifications_enabled");

        settingsToUpdate.put("inventory.enable_notifications", String.valueOf(notificationsEnabled));
        settingsToUpdate.put("email.notifications.enabled", String.valueOf(emailNotificationsEnabled));

        // Update settings
        settingService.updateSettings(settingsToUpdate);
        redirectAttributes.addFlashAttribute("successMessage", "Settings updated successfully");

        return "redirect:/settings";
    }

    @PostMapping("/reset")
    public String resetSettings(RedirectAttributes redirectAttributes) {
        settingService.resetToDefaults();
        redirectAttributes.addFlashAttribute("successMessage", "Settings reset to defaults");
        return "redirect:/settings";
    }
}