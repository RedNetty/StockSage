package com.portfolio.stocksage.validation;

import com.portfolio.stocksage.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

public class UniqueSkuValidator implements ConstraintValidator<UniqueSkuValidator.UniqueSku, String> {

    private final ProductRepository productRepository;

    @Autowired
    public UniqueSkuValidator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void initialize(UniqueSku constraintAnnotation) {
        // Initialization logic, if any
    }

    @Override
    public boolean isValid(String sku, ConstraintValidatorContext context) {
        if (sku == null || sku.isEmpty()) {
            return true; // Let @NotNull or @NotEmpty handle this validation
        }
        return !productRepository.existsBySku(sku);
    }

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = UniqueSkuValidator.class)
    public @interface UniqueSku {
        String message() default "SKU already exists";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}