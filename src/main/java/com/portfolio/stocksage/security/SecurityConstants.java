package com.portfolio.stocksage.security;

/**
 * Constants for security-related configurations
 */
public class SecurityConstants {

    // JWT token expiration time (24 hours in milliseconds)
    public static final long JWT_EXPIRATION_TIME = 86400000;

    // JWT secret key environment variable name
    public static final String JWT_SECRET_ENV_VARIABLE = "JWT_SECRET";

    // JWT token prefix
    public static final String TOKEN_PREFIX = "Bearer ";

    // Authorization header name
    public static final String HEADER_STRING = "Authorization";

    // User roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_INVENTORY_MANAGER = "INVENTORY_MANAGER";
    public static final String ROLE_USER = "USER";

    // Session timeout in seconds (30 minutes)
    public static final int SESSION_TIMEOUT = 1800;

    // Maximum login attempts before account lockout
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    // Account lockout duration in minutes
    public static final int ACCOUNT_LOCKOUT_DURATION = 30;

    // Password reset token expiration time in hours
    public static final int PASSWORD_RESET_TOKEN_EXPIRATION = 24;

    // Allowed origins for CORS
    public static final String[] ALLOWED_ORIGINS = {
            "http://localhost:8080",
            "http://localhost:3000",
            "https://stocksage.com"
    };

    // Secured API endpoints pattern
    public static final String SECURED_API_PATTERN = "/api/**";

    // Public API endpoints
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/signup",
            "/api/auth/password-reset",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    // Static resource patterns
    public static final String[] STATIC_RESOURCES = {
            "/",
            "/favicon.ico",
            "/css/**",
            "/js/**",
            "/images/**",
            "/webjars/**"
    };

    // Public web pages
    public static final String[] PUBLIC_PAGES = {
            "/login",
            "/register",
            "/home",
            "/about",
            "/contact",
            "/error"
    };

    // Private constructor to prevent instantiation
    private SecurityConstants() {
        throw new AssertionError("SecurityConstants is a utility class and should not be instantiated");
    }
}