package com.portfolio.stocksage.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * Configure the task executor for asynchronous methods
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("Creating Async Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("StockSage-");
        executor.initialize();
        return executor;
    }

    /**
     * Configure a dedicated task executor for notification processing
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        log.info("Creating Notification Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("Notification-");
        executor.initialize();
        return executor;
    }

    /**
     * Configure a dedicated task executor for email sending
     */
    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        log.info("Creating Email Task Executor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(15);
        executor.setThreadNamePrefix("Email-");
        executor.initialize();
        return executor;
    }
}