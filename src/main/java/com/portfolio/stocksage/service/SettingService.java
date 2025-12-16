package com.portfolio.stocksage.service;

import java.util.Map;

public interface SettingService {

    /**
     * Get all application settings
     */
    Map<String, String> getAllSettings();

    /**
     * Get a setting by key
     *
     * @param key Setting key
     * @return Setting value, or null if not found
     */
    String getSetting(String key);

    /**
     * Get a setting by key with a default value if not found
     *
     * @param key Setting key
     * @param defaultValue Default value if setting not found
     * @return Setting value or default value
     */
    String getSetting(String key, String defaultValue);

    /**
     * Update a setting
     *
     * @param key Setting key
     * @param value New setting value
     */
    void updateSetting(String key, String value);

    /**
     * Update multiple settings at once
     *
     * @param settings Map of setting keys and values
     */
    void updateSettings(Map<String, String> settings);

    /**
     * Reset all settings to their default values
     */
    void resetToDefaults();

    /**
     * Delete a setting
     *
     * @param key Setting key
     */
    void deleteSetting(String key);

    /**
     * Get the low stock threshold value
     */
    int getLowStockThreshold();

    /**
     * Check if inventory notifications are enabled
     */
    boolean isNotificationsEnabled();

    /**
     * Check if email notifications are enabled
     */
    boolean isEmailNotificationsEnabled();
}