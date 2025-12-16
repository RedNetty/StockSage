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

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmailValidator.UniqueEmail, String> {

    private final UserRepository userRepository;

    @Autowired
    public UniqueEmailValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
        // Initialization logic, if needed
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isEmpty()) {
            return true; // Let @NotNull or @NotEmpty handle this validation
        }
        return !userRepository.existsByEmail(email);
    }

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = UniqueEmailValidator.class)
    public @interface UniqueEmail {
        String message() default "Email address already exists";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}