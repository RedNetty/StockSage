package com.portfolio.stocksage.validation;

import com.portfolio.stocksage.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

public class UniqueCategoryNameValidator implements ConstraintValidator<UniqueCategoryNameValidator.UniqueCategoryName, String> {

    private final CategoryRepository categoryRepository;

    @Autowired
    public UniqueCategoryNameValidator(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void initialize(UniqueCategoryName constraintAnnotation) {
        // Initialization logic, if needed
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.isEmpty()) {
            return true; // Let @NotNull or @NotEmpty handle this validation
        }
        return !categoryRepository.existsByName(name);
    }

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = UniqueCategoryNameValidator.class)
    public @interface UniqueCategoryName {
        String message() default "Category name already exists";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}