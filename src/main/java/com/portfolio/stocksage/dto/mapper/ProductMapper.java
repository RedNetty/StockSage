package com.portfolio.stocksage.dto.mapper;

import com.portfolio.stocksage.dto.request.ProductCreateDTO;
import com.portfolio.stocksage.dto.response.ProductDTO;
import com.portfolio.stocksage.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "suppliers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductCreateDTO dto);

    @Mapping(target = "category.id", source = "category.id")
    @Mapping(target = "category.name", source = "category.name")
    ProductDTO toDto(Product entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "suppliers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(ProductCreateDTO dto, @MappingTarget Product entity);
}