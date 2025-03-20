package com.portfolio.stocksage.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for storing application settings
 */
@Entity
@Table(name = "settings", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "scope"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Setting name/key
     */
    @Column(nullable = false)
    private String name;

    /**
     * Setting value
     */
    @Column(columnDefinition = "TEXT")
    private String value;

    /**
     * Setting data type
     */
    @Column(name = "data_type")
    private String dataType;

    /**
     * Setting description
     */
    @Column
    private String description;

    /**
     * Setting scope (global, user-specific, etc.)
     */
    @Column
    private String scope;

    /**
     * Whether this setting is visible in the UI
     */
    @Column(name = "is_visible")
    private boolean visible;

    /**
     * Display order in the UI
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Convert the value to the appropriate type based on data_type
     */
    public Object getTypedValue() {
        if (value == null) {
            return null;
        }

        switch (dataType) {
            case "Integer":
                return Integer.parseInt(value);
            case "Long":
                return Long.parseLong(value);
            case "Double":
                return Double.parseDouble(value);
            case "Boolean":
                return Boolean.parseBoolean(value);
            default:
                return value;
        }
    }

    /**
     * Set the value, converting from the appropriate type
     */
    public void setTypedValue(Object typedValue) {
        if (typedValue == null) {
            this.value = null;
            return;
        }

        this.value = typedValue.toString();

        // Set the dataType based on the object class
        if (typedValue instanceof Integer) {
            this.dataType = "Integer";
        } else if (typedValue instanceof Long) {
            this.dataType = "Long";
        } else if (typedValue instanceof Double) {
            this.dataType = "Double";
        } else if (typedValue instanceof Boolean) {
            this.dataType = "Boolean";
        } else {
            this.dataType = "String";
        }
    }
}