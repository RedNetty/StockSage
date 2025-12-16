package com.portfolio.stocksage.repository;

import com.portfolio.stocksage.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {

    Optional<Setting> findByKey(String key);

    boolean existsByKey(String key);
}