package com.portfolio.stocksage.controller.api;

import com.portfolio.stocksage.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Settings API", description = "Endpoints for managing application settings")
public class SettingController {

    private final SettingService settingService;

    @GetMapping
    @Operation(summary = "Get all settings", description = "Returns all application settings")
    public ResponseEntity<Map<String, String>> getAllSettings() {
        Map<String, String> settings = settingService.getAllSettings();
        return ResponseEntity.ok(settings);
    }

    @GetMapping("/{key}")
    @Operation(summary = "Get setting by key", description = "Returns a specific setting by its key")
    public ResponseEntity<String> getSetting(
            @Parameter(description = "Setting key", required = true)
            @PathVariable String key) {
        String value = settingService.getSetting(key);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(value);
    }

    @PutMapping("/{key}")
    @Operation(summary = "Update setting", description = "Updates a specific setting")
    public ResponseEntity<Void> updateSetting(
            @Parameter(description = "Setting key", required = true)
            @PathVariable String key,
            @Parameter(description = "Setting value", required = true)
            @RequestBody String value) {
        settingService.updateSetting(key, value);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    @Operation(summary = "Update multiple settings", description = "Updates multiple settings at once")
    public ResponseEntity<Void> updateSettings(
            @Parameter(description = "Map of setting keys and values", required = true)
            @RequestBody Map<String, String> settings) {
        settingService.updateSettings(settings);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset settings to defaults", description = "Resets all settings to their default values")
    public ResponseEntity<Void> resetToDefaults() {
        settingService.resetToDefaults();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{key}")
    @Operation(summary = "Delete setting", description = "Deletes a specific setting")
    public ResponseEntity<Void> deleteSetting(
            @Parameter(description = "Setting key", required = true)
            @PathVariable String key) {
        settingService.deleteSetting(key);
        return ResponseEntity.noContent().build();
    }
}