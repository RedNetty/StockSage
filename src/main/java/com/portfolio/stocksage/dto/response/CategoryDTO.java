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
public class CategoryDTO {

    private Long id;
    private String name;
    private String description;
    private CategoryDTO parent;
    private List<CategoryDTO> subCategories = new ArrayList<>();
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long productCount;
}