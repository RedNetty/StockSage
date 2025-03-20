package com.portfolio.stocksage.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Define cache manager with Caffeine
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats());

        // Define all cache names used in the application
        cacheManager.setCacheNames(Arrays.asList(
                "products",
                "categories",
                "suppliers",
                "warehouses",
                "inventorySummary",
                "lowStockItems",
                "outOfStockItems",
                "topSellingProducts",
                "userRoles"
        ));

        return cacheManager;
    }
}