@echo off
REM StockSage Project Setup Script for Windows
REM This script creates the entire file structure and Java files for the StockSage project

echo Setting up StockSage Inventory Management System...

REM Create root project directory
mkdir stock-sage
cd stock-sage

REM Create .github directory and workflows
mkdir .github\workflows
(
echo name: Java CI with Maven
echo.
echo on:
echo   push:
echo     branches: [ main ]
echo   pull_request:
echo     branches: [ main ]
echo.
echo jobs:
echo   build:
echo     runs-on: ubuntu-latest
echo     steps:
echo     - uses: actions/checkout@v2
echo     - name: Set up JDK 17
echo       uses: actions/setup-java@v2
echo       with:
echo         java-version: '17'
echo         distribution: 'adopt'
echo     - name: Build with Maven
echo       run: ./mvnw clean verify
) > .github\workflows\ci.yml

REM Create Maven wrapper directory
mkdir .mvn\wrapper

REM Create Docker directory
mkdir docker\postgres

REM Create init.sql for PostgreSQL
(
echo -- This will be executed when the PostgreSQL container is created
echo CREATE DATABASE stocksage;
echo \c stocksage;
echo.
echo -- Any additional initialization can be added here
) > docker\postgres\init.sql

REM Create main source directories
mkdir src\main\java\com\portfolio\stocksage\config
mkdir src\main\java\com\portfolio\stocksage\controller\api
mkdir src\main\java\com\portfolio\stocksage\controller\web
mkdir src\main\java\com\portfolio\stocksage\dto\request
mkdir src\main\java\com\portfolio\stocksage\dto\response
mkdir src\main\java\com\portfolio\stocksage\dto\mapper
mkdir src\main\java\com\portfolio\stocksage\entity
mkdir src\main\java\com\portfolio\stocksage\exception
mkdir src\main\java\com\portfolio\stocksage\repository
mkdir src\main\java\com\portfolio\stocksage\service\impl
mkdir src\main\java\com\portfolio\stocksage\security
mkdir src\main\java\com\portfolio\stocksage\util
mkdir src\main\java\com\portfolio\stocksage\validation

REM Create resource directories
mkdir src\main\resources\db\migration
mkdir src\main\resources\static\css
mkdir src\main\resources\static\js
mkdir src\main\resources\static\images
mkdir src\main\resources\templates\fragments
mkdir src\main\resources\templates\layout
mkdir src\main\resources\templates\product
mkdir src\main\resources\templates\inventory

REM Create test directories
mkdir src\test\java\com\portfolio\stocksage\controller
mkdir src\test\java\com\portfolio\stocksage\repository
mkdir src\test\java\com\portfolio\stocksage\service
mkdir src\test\java\com\portfolio\stocksage\integration

REM Create uploads and logs directories
mkdir uploads
mkdir logs

REM Create Main Application Class
(
echo package com.portfolio.stocksage;
echo.
echo import org.springframework.boot.SpringApplication;
echo import org.springframework.boot.autoconfigure.SpringBootApplication;
echo import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
echo.
echo @SpringBootApplication
echo @EnableJpaAuditing
echo public class StockSageApplication {
echo.
echo     public static void main^(String[] args^) {
echo         SpringApplication.run^(StockSageApplication.class, args^);
echo     }
echo }
) > src\main\java\com\portfolio\stocksage\StockSageApplication.java

REM Create Configuration Classes
(
echo package com.portfolio.stocksage.config;
echo.
echo import io.swagger.v3.oas.models.Components;
echo import io.swagger.v3.oas.models.OpenAPI;
echo import io.swagger.v3.oas.models.info.Contact;
echo import io.swagger.v3.oas.models.info.Info;
echo import io.swagger.v3.oas.models.info.License;
echo import io.swagger.v3.oas.models.security.SecurityRequirement;
echo import io.swagger.v3.oas.models.security.SecurityScheme;
echo import org.springframework.context.annotation.Bean;
echo import org.springframework.context.annotation.Configuration;
echo.
echo @Configuration
echo public class OpenApiConfig {
echo.
echo     @Bean
echo     public OpenAPI customOpenAPI^(^) {
echo         return new OpenAPI^(^)
echo                 .info^(new Info^(^)
echo                         .title^("StockSage API"^)
echo                         .version^("1.0"^)
echo                         .description^("REST API for StockSage Inventory Management System"^)
echo                         .termsOfService^("https://stocksage.example.com/terms"^)
echo                         .contact^(new Contact^(^)
echo                                 .name^("StockSage Support"^)
echo                                 .url^("https://stocksage.example.com"^)
echo                                 .email^("support@stocksage.example.com"^)^)
echo                         .license^(new License^(^)
echo                                 .name^("MIT License"^)
echo                                 .url^("https://opensource.org/licenses/MIT"^)^)^)
echo                 .addSecurityItem^(new SecurityRequirement^(^).addList^("bearerAuth"^)^)
echo                 .components^(new Components^(^)
echo                         .addSecuritySchemes^("bearerAuth",
echo                                 new SecurityScheme^(^)
echo                                         .name^("bearerAuth"^)
echo                                         .type^(SecurityScheme.Type.HTTP^)
echo                                         .scheme^("bearer"^)
echo                                         .bearerFormat^("JWT"^)^)^);
echo     }
echo }
) > src\main\java\com\portfolio\stocksage\config\OpenApiConfig.java

REM Create Entity Classes
(
echo package com.portfolio.stocksage.entity;
echo.
echo import lombok.AllArgsConstructor;
echo import lombok.Builder;
echo import lombok.Data;
echo import lombok.NoArgsConstructor;
echo import org.hibernate.annotations.CreationTimestamp;
echo import org.hibernate.annotations.UpdateTimestamp;
echo.
echo import javax.persistence.*;
echo import java.math.BigDecimal;
echo import java.time.LocalDateTime;
echo import java.util.HashSet;
echo import java.util.Set;
echo.
echo @Entity
echo @Table^(name = "products"^)
echo @Data
echo @NoArgsConstructor
echo @AllArgsConstructor
echo @Builder
echo public class Product {
echo.
echo     @Id
echo     @GeneratedValue^(strategy = GenerationType.IDENTITY^)
echo     private Long id;
echo.
echo     @Column^(nullable = false, unique = true^)
echo     private String sku;
echo.
echo     @Column^(nullable = false^)
echo     private String name;
echo.
echo     @Column^(length = 1000^)
echo     private String description;
echo.
echo     @Column^(nullable = false, precision = 10, scale = 2^)
echo     private BigDecimal unitPrice;
echo.
echo     @ManyToOne^(fetch = FetchType.LAZY^)
echo     @JoinColumn^(name = "category_id", nullable = false^)
echo     private Category category;
echo.
echo     private String imageUrl;
echo.
echo     private boolean active;
echo.
echo     @Column^(name = "units_in_stock"^)
echo     private Integer unitsInStock;
echo.
echo     @OneToMany^(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true^)
echo     private Set^<Inventory^> inventories = new HashSet^<^>^(^);
echo.
echo     @OneToMany^(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true^)
echo     private Set^<Transaction^> transactions = new HashSet^<^>^(^);
echo.
echo     @ManyToMany
echo     @JoinTable^(
echo         name = "product_supplier",
echo         joinColumns = @JoinColumn^(name = "product_id"^),
echo         inverseJoinColumns = @JoinColumn^(name = "supplier_id"^)
echo     ^)
echo     private Set^<Supplier^> suppliers = new HashSet^<^>^(^);
echo.
echo     @CreationTimestamp
echo     @Column^(name = "created_at", updatable = false^)
echo     private LocalDateTime createdAt;
echo.
echo     @UpdateTimestamp
echo     @Column^(name = "updated_at"^)
echo     private LocalDateTime updatedAt;
echo.
echo     public void addSupplier^(Supplier supplier^) {
echo         this.suppliers.add^(supplier^);
echo         supplier.getProducts^(^).add^(this^);
echo     }
echo.
echo     public void removeSupplier^(Supplier supplier^) {
echo         this.suppliers.remove^(supplier^);
echo         supplier.getProducts^(^).remove^(this^);
echo     }
echo.
echo     public void addInventory^(Inventory inventory^) {
echo         this.inventories.add^(inventory^);
echo         inventory.setProduct^(this^);
echo     }
echo.
echo     public void removeInventory^(Inventory inventory^) {
echo         this.inventories.remove^(inventory^);
echo         inventory.setProduct^(null^);
echo     }
echo }
) > src\main\java\com\portfolio\stocksage\entity\Product.java

REM Create Category Entity
(
echo package com.portfolio.stocksage.entity;
echo.
echo import lombok.AllArgsConstructor;
echo import lombok.Builder;
echo import lombok.Data;
echo import lombok.NoArgsConstructor;
echo import org.hibernate.annotations.CreationTimestamp;
echo import org.hibernate.annotations.UpdateTimestamp;
echo.
echo import javax.persistence.*;
echo import java.time.LocalDateTime;
echo import java.util.HashSet;
echo import java.util.Set;
echo.
echo @Entity
echo @Table^(name = "categories"^)
echo @Data
echo @NoArgsConstructor
echo @AllArgsConstructor
echo @Builder
echo public class Category {
echo.
echo     @Id
echo     @GeneratedValue^(strategy = GenerationType.IDENTITY^)
echo     private Long id;
echo.
echo     @Column^(nullable = false, unique = true^)
echo     private String name;
echo.
echo     private String description;
echo.
echo     @ManyToOne^(fetch = FetchType.LAZY^)
echo     @JoinColumn^(name = "parent_id"^)
echo     private Category parent;
echo.
echo     @OneToMany^(mappedBy = "parent", cascade = CascadeType.ALL^)
echo     private Set^<Category^> subCategories = new HashSet^<^>^(^);
echo.
echo     @OneToMany^(mappedBy = "category", cascade = CascadeType.ALL^)
echo     private Set^<Product^> products = new HashSet^<^>^(^);
echo.
echo     private boolean active;
echo.
echo     @CreationTimestamp
echo     @Column^(name = "created_at", updatable = false^)
echo     private LocalDateTime createdAt;
echo.
echo     @UpdateTimestamp
echo     @Column^(name = "updated_at"^)
echo     private LocalDateTime updatedAt;
echo.
echo     public void addSubCategory^(Category subCategory^) {
echo         this.subCategories.add^(subCategory^);
echo         subCategory.setParent^(this^);
echo     }
echo.
echo     public void removeSubCategory^(Category subCategory^) {
echo         this.subCategories.remove^(subCategory^);
echo         subCategory.setParent^(null^);
echo     }
echo.
echo     public void addProduct^(Product product^) {
echo         this.products.add^(product^);
echo         product.setCategory^(this^);
echo     }
echo.
echo     public void removeProduct^(Product product^) {
echo         this.products.remove^(product^);
echo         product.setCategory^(null^);
echo     }
echo }
) > src\main\java\com\portfolio\stocksage\entity\Category.java

REM Create Warehouse Entity
(
echo package com.portfolio.stocksage.entity;
echo.
echo import lombok.AllArgsConstructor;
echo import lombok.Builder;
echo import lombok.Data;
echo import lombok.NoArgsConstructor;
echo import org.hibernate.annotations.CreationTimestamp;
echo import org.hibernate.annotations.UpdateTimestamp;
echo.
echo import javax.persistence.*;
echo import java.time.LocalDateTime;
echo import java.util.HashSet;
echo import java.util.Set;
echo.
echo @Entity
echo @Table^(name = "warehouses"^)
echo @Data
echo @NoArgsConstructor
echo @AllArgsConstructor
echo @Builder
echo public class Warehouse {
echo.
echo     @Id
echo     @GeneratedValue^(strategy = GenerationType.IDENTITY^)
echo     private Long id;
echo.
echo     @Column^(nullable = false, unique = true^)
echo     private String name;
echo.
echo     @Column^(nullable = false^)
echo     private String location;
echo.
echo     private String description;
echo.
echo     private Double capacity;
echo.
echo     private Boolean active;
echo.
echo     @OneToMany^(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true^)
echo     private Set^<Inventory^> inventories = new HashSet^<^>^(^);
echo.
echo     @CreationTimestamp
echo     @Column^(name = "created_at", updatable = false^)
echo     private LocalDateTime createdAt;
echo.
echo     @UpdateTimestamp
echo     @Column^(name = "updated_at"^)
echo     private LocalDateTime updatedAt;
echo }
) > src\main\java\com\portfolio\stocksage\entity\Warehouse.java

REM Create application.properties
(
echo # Common application properties
echo spring.application.name=StockSage
echo server.port=8080
echo.
echo # Database configuration
echo spring.datasource.url=jdbc:postgresql://localhost:5432/stocksage
echo spring.datasource.username=postgres
echo spring.datasource.password=postgres
echo spring.datasource.driver-class-name=org.postgresql.Driver
echo.
echo # JPA/Hibernate properties
echo spring.jpa.hibernate.ddl-auto=validate
echo spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
echo spring.jpa.properties.hibernate.format_sql=true
echo spring.jpa.show-sql=true
echo.
echo # Flyway migration configuration
echo spring.flyway.enabled=true
echo spring.flyway.locations=classpath:db/migration
echo spring.flyway.baseline-on-migrate=true
echo.
echo # Jackson configuration
echo spring.jackson.serialization.write-dates-as-timestamps=false
echo spring.jackson.date-format=yyyy-MM-dd HH:mm:ss
echo spring.jackson.time-zone=UTC
echo.
echo # Security configuration
echo jwt.secret=stocksageJwtSecretKey
echo jwt.expiration=86400000
echo.
echo # Logging configuration
echo logging.level.root=INFO
echo logging.level.com.portfolio.stocksage=DEBUG
echo logging.level.org.springframework.web=INFO
echo logging.level.org.hibernate=ERROR
echo logging.file.name=logs/stocksage.log
) > src\main\resources\application.properties

REM Create .gitignore
(
echo # Compiled class files
echo *.class
echo.
echo # Log files
echo *.log
echo logs/
echo.
echo # Package Files
echo *.jar
echo *.war
echo *.nar
echo *.ear
echo *.zip
echo *.tar.gz
echo *.rar
echo.
echo # Maven
echo target/
echo pom.xml.tag
echo pom.xml.releaseBackup
echo pom.xml.versionsBackup
echo pom.xml.next
echo release.properties
echo dependency-reduced-pom.xml
echo buildNumber.properties
echo.
echo # Eclipse
echo .classpath
echo .project
echo .settings/
echo.
echo # IntelliJ IDEA
echo .idea/
echo *.iws
echo *.iml
echo *.ipr
echo.
echo # VS Code
echo .vscode/
echo.
echo # Application specific
echo application-local.properties
echo uploads/
) > .gitignore

REM Create pom.xml
(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<project xmlns="http://maven.apache.org/POM/4.0.0"
echo          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
echo          xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd"^>
echo     ^<modelVersion^>4.0.0^</modelVersion^>
echo
echo     ^<parent^>
echo         ^<groupId^>org.springframework.boot^</groupId^>
echo         ^<artifactId^>spring-boot-starter-parent^</artifactId^>
echo         ^<version^>2.7.14^</version^>
echo         ^<relativePath/^> ^<!-- lookup parent from repository --^>
echo     ^</parent^>
echo
echo     ^<groupId^>com.portfolio^</groupId^>
echo     ^<artifactId^>stock-sage^</artifactId^>
echo     ^<version^>1.0.0^</version^>
echo     ^<name^>StockSage^</name^>
echo     ^<description^>Advanced Inventory Management System built with Spring Boot^</description^>
echo
echo     ^<properties^>
echo         ^<java.version^>17^</java.version^>
echo         ^<maven.compiler.source^>${java.version}^</maven.compiler.source^>
echo         ^<maven.compiler.target^>${java.version}^</maven.compiler.target^>
echo         ^<project.build.sourceEncoding^>UTF-8^</project.build.sourceEncoding^>
echo         ^<project.reporting.outputEncoding^>UTF-8^</project.reporting.outputEncoding^>
echo
echo         ^<!-- Dependency versions --^>
echo         ^<mapstruct.version^>1.5.3.Final^</mapstruct.version^>
echo         ^<lombok.version^>1.18.26^</lombok.version^>
echo         ^<springdoc.version^>1.6.15^</springdoc.version^>
echo         ^<jjwt.version^>0.11.5^</jjwt.version^>
echo     ^</properties^>
echo
echo     ^<dependencies^>
echo         ^<!-- Spring Boot Starters --^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-data-jpa^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-web^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-validation^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-security^</artifactId^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-thymeleaf^</artifactId^>
echo         ^</dependency^>
echo
echo         ^<!-- Database --^>
echo         ^<dependency^>
echo             ^<groupId^>org.postgresql^</groupId^>
echo             ^<artifactId^>postgresql^</artifactId^>
echo             ^<scope^>runtime^</scope^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.flywaydb^</groupId^>
echo             ^<artifactId^>flyway-core^</artifactId^>
echo         ^</dependency^>
echo
echo         ^<!-- Utilities --^>
echo         ^<dependency^>
echo             ^<groupId^>org.projectlombok^</groupId^>
echo             ^<artifactId^>lombok^</artifactId^>
echo             ^<version^>${lombok.version}^</version^>
echo             ^<optional^>true^</optional^>
echo         ^</dependency^>
echo         ^<dependency^>
echo             ^<groupId^>org.mapstruct^</groupId^>
echo             ^<artifactId^>mapstruct^</artifactId^>
echo             ^<version^>${mapstruct.version}^</version^>
echo         ^</dependency^>
echo
echo         ^<!-- Testing --^>
echo         ^<dependency^>
echo             ^<groupId^>org.springframework.boot^</groupId^>
echo             ^<artifactId^>spring-boot-starter-test^</artifactId^>
echo             ^<scope^>test^</scope^>
echo         ^</dependency^>
echo     ^</dependencies^>
echo
echo     ^<build^>
echo         ^<plugins^>
echo             ^<plugin^>
echo                 ^<groupId^>org.springframework.boot^</groupId^>
echo                 ^<artifactId^>spring-boot-maven-plugin^</artifactId^>
echo             ^</plugin^>
echo         ^</plugins^>
echo     ^</build^>
echo ^</project^>
) > pom.xml

REM Create README.md
(
echo # StockSage
echo.
echo ## Modern Inventory Management Solution
echo.
echo StockSage is a robust, enterprise-grade inventory management system built with Java, Spring Boot, and SQL. This application demonstrates software architecture best practices and provides a comprehensive solution for tracking inventory across multiple warehouses, managing suppliers, and recording detailed transaction history.
echo.
echo ## Key Features
echo.
echo - **Comprehensive Product Management:** Create, read, update, and delete products with detailed attributes
echo - **Category Hierarchy:** Organize products with a flexible multi-level category system
echo - **Multi-Warehouse Support:** Track inventory levels across multiple physical locations
echo - **Robust Transaction Logging:** Keep detailed records of all inventory movements
echo - **Supplier Management:** Maintain supplier information and link products to suppliers
echo - **User Authentication & Authorization:** Secure role-based access control
echo - **RESTful API:** Full API coverage with OpenAPI documentation
echo.
echo ## Technology Stack
echo.
echo ### Backend
echo - **Java 17**
echo - **Spring Boot 2.7.x**
echo - **Spring Data JPA**
echo - **Spring Security**
echo - **Hibernate**
echo - **Flyway Migration**
echo.
echo ### Database
echo - **PostgreSQL**
) > README.md

REM Create Dockerfile
(
echo FROM openjdk:17-jdk-slim as build
echo.
echo WORKDIR /app
echo.
echo COPY mvnw .
echo COPY .mvn .mvn
echo COPY pom.xml .
echo.
echo RUN chmod +x ./mvnw
echo RUN ./mvnw dependency:go-offline -B
echo.
echo COPY src src
echo.
echo RUN ./mvnw package -DskipTests
echo RUN mkdir -p target/dependency && ^(cd target/dependency; jar -xf ../*.jar^)
echo.
echo FROM openjdk:17-jdk-slim
echo.
echo ARG DEPENDENCY=/app/target/dependency
echo.
echo COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
echo COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
echo COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
echo.
echo ENTRYPOINT ["java", "-cp", "app:app/lib/*", "com.portfolio.stocksage.StockSageApplication"]
) > Dockerfile

REM Create docker-compose.yml
(
echo version: '3.8'
echo.
echo services:
echo   app:
echo     build: .
echo     container_name: stocksage-app
echo     depends_on:
echo       - db
echo     ports:
echo       - "8080:8080"
echo     environment:
echo       - SPRING_PROFILES_ACTIVE=prod
echo       - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/stocksage
echo       - SPRING_DATASOURCE_USERNAME=postgres
echo       - SPRING_DATASOURCE_PASSWORD=postgres
echo     volumes:
echo       - ./uploads:/app/uploads
echo     networks:
echo       - stocksage-network
echo     restart: unless-stopped
echo.
echo   db:
echo     image: postgres:14-alpine
echo     container_name: stocksage-db
echo     ports:
echo       - "5432:5432"
echo     environment:
echo       - POSTGRES_DB=stocksage
echo       - POSTGRES_USER=postgres
echo       - POSTGRES_PASSWORD=postgres
echo     volumes:
echo       - postgres-data:/var/lib/postgresql/data
echo       - ./docker/postgres:/docker-entrypoint-initdb.d
echo     networks:
echo       - stocksage-network
echo     restart: unless-stopped
echo.
echo volumes:
echo   postgres-data:
echo.
echo networks:
echo   stocksage-network:
echo     driver: bridge
) > docker-compose.yml

REM Create DB migration script
(
echo -- V1__init_schema.sql
echo.
echo -- Create sequences
echo CREATE SEQUENCE IF NOT EXISTS hibernate_sequence START 1 INCREMENT 1;
echo.
echo -- Role table
echo CREATE TABLE roles ^(
echo     id BIGSERIAL PRIMARY KEY,
echo     name VARCHAR^(20^) NOT NULL UNIQUE,
echo     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
echo     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
echo ^);
echo.
echo -- User table
echo CREATE TABLE users ^(
echo     id BIGSERIAL PRIMARY KEY,
echo     username VARCHAR^(50^) NOT NULL UNIQUE,
echo     email VARCHAR^(100^) NOT NULL UNIQUE,
echo     password VARCHAR^(100^) NOT NULL,
echo     first_name VARCHAR^(50^) NOT NULL,
echo     last_name VARCHAR^(50^) NOT NULL,
echo     active BOOLEAN NOT NULL DEFAULT TRUE,
echo     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
echo     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
echo ^);
echo.
echo -- Category table
echo CREATE TABLE categories ^(
echo     id BIGSERIAL PRIMARY KEY,
echo     name VARCHAR^(100^) NOT NULL UNIQUE,
echo     description TEXT,
echo     parent_id BIGINT,
echo     active BOOLEAN NOT NULL DEFAULT TRUE,
echo     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
echo     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
echo     FOREIGN KEY ^(parent_id^) REFERENCES categories^(id^) ON DELETE SET NULL
echo ^);
echo.
echo -- Product table
echo CREATE TABLE products ^(
echo     id BIGSERIAL PRIMARY KEY,
echo     sku VARCHAR^(50^) NOT NULL UNIQUE,
echo     name VARCHAR^(100^) NOT NULL,
echo     description TEXT,
echo     unit_price DECIMAL^(10,2^) NOT NULL,
echo     category_id BIGINT NOT NULL,
echo     image_url VARCHAR^(255^),
echo     active BOOLEAN NOT NULL DEFAULT TRUE,
echo     units_in_stock INTEGER NOT NULL DEFAULT 0,
echo     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
echo     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
echo     FOREIGN KEY ^(category_id^) REFERENCES categories^(id^)
echo ^);
) > src\main\resources\db\migration\V1__init_schema.sql

echo Project structure has been created successfully.
echo Use 'cd stock-sage' to navigate to the project root.