package com.portfolio.stocksage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierCreateDTO {

    @NotBlank(message = "Supplier name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 100, message = "Contact name cannot exceed 100 characters")
    private String contactName;

    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phone;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Size(max = 50, message = "Tax ID cannot exceed 50 characters")
    private String taxId;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private boolean active = true;
}