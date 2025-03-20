package com.portfolio.stocksage.service;

import com.portfolio.stocksage.entity.Setting;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing application settings
 */
public interface SettingService {

    /**
     * Get a setting by ID
     */
    Setting getSettingById(Long id);

    /**
     * Get a setting by name
     */
    Optional<Setting> getSettingByName(String name);

    /**
     * Get a setting by name and scope
     */
    Optional<Setting> getSettingByNameAndScope(String name, String scope);

    /**
     * Get a setting value by name, returning a default value if not found
     */
    <T> T getSettingValue(String name, T defaultValue);

    /**
     * Get a setting value by name and scope, returning a default value if not found
     */
    <T> T getSettingValue(String name, String scope, T defaultValue);

    /**
     * Save a setting
     */
    Setting saveSetting(Setting setting);

    /**
     * Save a setting with name, value, and scope
     */
    Setting saveSetting(String name, Object value, String scope);

    /**
     * Delete a setting
     */
    void deleteSetting(Long id);

    /**
     * Delete a setting by name
     */
    void deleteSettingByName(String name);

    /**
     * Delete a setting by name and scope
     */
    void deleteSettingByNameAndScope(String name, String scope);

    /**
     * Get all settings
     */
    List<Setting> getAllSettings();

    /**
     * Get settings by scope
     */
    List<Setting> getSettingsByScope(String scope);

    /**
     * Get visible settings
     */
    List<Setting> getVisibleSettings();

    /**
     * Get visible settings by scope
     */
    List<Setting> getVisibleSettingsByScope(String scope);

    /**
     * Get settings as a map of name to value
     */
    Map<String, Object> getSettingsAsMap();

    /**
     * Get settings as a map of name to value by scope
     */
    Map<String, Object> getSettingsAsMapByScope(String scope);

    /**
     * Reload settings from the database
     */
    void reloadSettings();

    /**
     * Initialize default settings
     */
    void initializeDefaultSettings();
}