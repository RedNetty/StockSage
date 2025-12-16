package com.portfolio.stocksage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StockSageApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockSageApplication.class, args);
    }
}
