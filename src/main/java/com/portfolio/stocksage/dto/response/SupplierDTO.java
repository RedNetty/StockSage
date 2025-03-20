
package com.portfolio.stocksage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {

    private Long id;
    private String name;
    private String contactName;
    private String email;
    private String phone;
    private String address;
    private String taxId;
    private String notes;
    private boolean active;
    private List<ProductSummaryDTO> products = new ArrayList<>();
    private int productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}