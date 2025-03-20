package com.portfolio.stocksage.service.impl;

import com.portfolio.stocksage.entity.Setting;
import com.portfolio.stocksage.exception.ResourceNotFoundException;
import com.portfolio.stocksage.repository.SettingRepository;
import com.portfolio.stocksage.service.SettingService;
import com.portfolio.stocksage.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of SettingService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SettingServiceImpl implements SettingService {

    private final SettingRepository settingRepository;

    private Map<String, String> defaultSettings;

    @PostConstruct
    public void init() {
        // Initialize default settings
        defaultSettings = new HashMap<>();
        defaultSettings.put("app.name", "StockSage");
        defaultSettings.put("app.version", "1.0.0");
        defaultSettings.put("inventory.low_stock_threshold", "10");
        defaultSettings.put("inventory.enable_notifications", "true");
        defaultSettings.put("transaction.auto_complete", "false");
        defaultSettings.put("transaction.require_approval", "false");
        defaultSettings.put("email.notifications.enabled", "false");
        defaultSettings.put("reports.default_format", "PDF");

        // Initialize settings in database if they don't exist
        initializeSettings();
    }

    /**
     * Initialize settings in the database with default values if they don't exist
     */
    @Transactional
    public void initializeSettings() {
        log.info("Initializing application settings");

        for (Map.Entry<String, String> entry : defaultSettings.entrySet()) {
            String key = entry.getKey();
            String defaultValue = entry.getValue();

            Optional<Setting> existingSetting = settingRepository.findByKey(key);
            if (existingSetting.isEmpty()) {
                Setting setting = new Setting();
                setting.setKey(key);
                setting.setValue(defaultValue);
                setting.setDescription(getDescriptionForKey(key));

                settingRepository.save(setting);
                log.info("Created default setting: {} = {}", key, defaultValue);
            }
        }
    }

    /**
     * Get description for setting key
     */
    private String getDescriptionForKey(String key) {
        switch (key) {
            case "app.name":
                return "Application name";
            case "app.version":
                return "Application version";
            case "inventory.low_stock_threshold":
                return "Threshold for low stock alerts";
            case "inventory.enable_notifications":
                return "Enable inventory-related notifications";
            case "transaction.auto_complete":
                return "Automatically mark transactions as completed";
            case "transaction.require_approval":
                return "Require approval for transactions";
            case "email.notifications.enabled":
                return "Enable email notifications";
            case "reports.default_format":
                return "Default format for report exports";
            default:
                return "System setting";
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "settings")
    public Map<String, String> getAllSettings() {
        List<Setting> settings = settingRepository.findAll();

        return settings.stream()
                .collect(Collectors.toMap(Setting::getKey, Setting::getValue));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "settings", key = "#key")
    public String getSetting(String key) {
        Optional<Setting> setting = settingRepository.findByKey(key);
        return setting.map(Setting::getValue)
                .orElseGet(() -> defaultSettings.getOrDefault(key, null));
    }

    @Override
    @Transactional(readOnly = true)
    public String getSetting(String key, String defaultValue) {
        String value = getSetting(key);
        return value != null ? value : defaultValue;
    }

    @Override
    @Transactional
    @CacheEvict(value = "settings", key = "#key")
    public void updateSetting(String key, String value) {
        Setting setting = settingRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));

        setting.setValue(value);
        settingRepository.save(setting);
        log.info("Updated setting: {} = {}", key, value);
    }

    @Override
    @Transactional
    @CacheEvict(value = "settings", allEntries = true)
    public void updateSettings(Map<String, String> settings) {
        settings.forEach(this::updateSetting);
    }

    @Override
    @Transactional
    @CacheEvict(value = "settings", allEntries = true)
    public void resetToDefaults() {
        List<Setting> allSettings = settingRepository.findAll();

        for (Setting setting : allSettings) {
            String key = setting.getKey();
            String defaultValue = defaultSettings.get(key);

            if (defaultValue != null) {
                setting.setValue(defaultValue);
                settingRepository.save(setting);
                log.info("Reset setting to default: {} = {}", key, defaultValue);
            }
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "settings", key = "#key")
    public void deleteSetting(String key) {
        Setting setting = settingRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found with key: " + key));

        settingRepository.delete(setting);
        log.info("Deleted setting: {}", key);
    }

    @Override
    @Transactional(readOnly = true)
    public int getLowStockThreshold() {
        String thresholdStr = getSetting("inventory.low_stock_threshold", "10");
        try {
            return Integer.parseInt(thresholdStr);
        } catch (NumberFormatException e) {
            log.warn("Invalid low stock threshold value: {}, using default", thresholdStr);
            return 10;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNotificationsEnabled() {
        String enabled = getSetting("inventory.enable_notifications", "true");
        return Boolean.parseBoolean(enabled);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailNotificationsEnabled() {
        String enabled = getSetting("email.notifications.enabled", "false");
        return Boolean.parseBoolean(enabled);
    }
}