package com.portfolio.stocksage.validation;

import com.portfolio.stocksage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsernameValidator.UniqueUsername, String> {

    private final UserRepository userRepository;

    @Autowired
    public UniqueUsernameValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void initialize(UniqueUsername constraintAnnotation) {
        // Initialization logic, if needed
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.isEmpty()) {
            return true; // Let @NotNull or @NotEmpty handle this validation
        }
        return !userRepository.existsByUsername(username);
    }

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = UniqueUsernameValidator.class)
    public @interface UniqueUsername {
        String message() default "Username already exists";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}