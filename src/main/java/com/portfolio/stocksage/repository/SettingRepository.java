package com.portfolio.stocksage.repository;

import com.portfolio.stocksage.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Setting entity
 */
@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

    /**
     * Find a setting by name
     */
    Optional<Setting> findByName(String name);

    /**
     * Find a setting by name and scope
     */
    Optional<Setting> findByNameAndScope(String name, String scope);

    /**
     * Check if a setting exists by name
     */
    boolean existsByName(String name);

    /**
     * Check if a setting exists by name and scope
     */
    boolean existsByNameAndScope(String name, String scope);

    /**
     * Find settings by scope
     */
    List<Setting> findByScope(String scope);

    /**
     * Find settings by visibility
     */
    List<Setting> findByVisible(boolean visible);

    /**
     * Find settings by scope and visibility
     */
    List<Setting> findByScopeAndVisible(String scope, boolean visible);

    /**
     * Find settings by name starting with a prefix
     */
    List<Setting> findByNameStartingWith(String prefix);

    /**
     * Find settings by name starting with a prefix and scope
     */
    List<Setting> findByNameStartingWithAndScope(String prefix, String scope);

    /**
     * Find visible settings ordered by display order
     */
    List<Setting> findByVisibleTrueOrderByDisplayOrderAsc();

    /**
     * Find visible settings by scope ordered by display order
     */
    List<Setting> findByVisibleTrueAndScopeOrderByDisplayOrderAsc(String scope);
}