package com.portfolio.stocksage.validation;

import com.portfolio.stocksage.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

public class UniqueWarehouseNameValidator implements ConstraintValidator<UniqueWarehouseNameValidator.UniqueWarehouseName, String> {

    private final WarehouseRepository warehouseRepository;

    @Autowired
    public UniqueWarehouseNameValidator(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    public void initialize(UniqueWarehouseName constraintAnnotation) {
        // Initialization logic, if needed
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.isEmpty()) {
            return true; // Let @NotNull or @NotEmpty handle this validation
        }
        return !warehouseRepository.existsByName(name);
    }

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = UniqueWarehouseNameValidator.class)
    public @interface UniqueWarehouseName {
        String message() default "Warehouse name already exists";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
}