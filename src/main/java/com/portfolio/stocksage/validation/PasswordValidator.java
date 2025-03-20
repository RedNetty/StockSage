package com.portfolio.stocksage.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<PasswordValidator.StrongPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;

    // Patterns for different password requirements
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_NUMBER = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL_CHAR = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        // Initialization, if needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        // Disable the default error message
        context.disableDefaultConstraintViolation();

        // Check length
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            context.buildConstraintViolationWithTemplate(
                    "Password must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters"
            ).addConstraintViolation();
            return false;
        }

        // Check for uppercase letters
        if (!HAS_UPPERCASE.matcher(password).find()) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one uppercase letter"
            ).addConstraintViolation();
            return false;
        }

        // Check for lowercase letters
        if (!HAS_LOWERCASE.matcher(password).find()) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one lowercase letter"
            ).addConstraintViolation();
            return false;
        }

        // Check for numbers
        if (!HAS_NUMBER.matcher(password).find()) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one number"
            ).addConstraintViolation();
            return false;
        }

        // Check for special characters
        if (!HAS_SPECIAL_CHAR.matcher(password).find()) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least one special character"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }

    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = PasswordValidator.class)
    public @interface StrongPassword {
        String message() default "Password does not meet security requirements";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}