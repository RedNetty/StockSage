package com.portfolio.stocksage.dto.mapper;

import com.portfolio.stocksage.dto.request.SupplierCreateDTO;
import com.portfolio.stocksage.dto.response.ProductSummaryDTO;
import com.portfolio.stocksage.dto.response.SupplierDTO;
import com.portfolio.stocksage.entity.Product;
import com.portfolio.stocksage.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Supplier toEntity(SupplierCreateDTO dto);

    @Mapping(target = "products", source = "products", qualifiedByName = "productsToProductSummaries")
    @Mapping(target = "productCount", expression = "java(supplier.getProducts().size())")
    SupplierDTO toDto(Supplier supplier);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(SupplierCreateDTO dto, @MappingTarget Supplier entity);

    @Named("productsToProductSummaries")
    default List<ProductSummaryDTO> productsToProductSummaries(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream()
                .map(product -> ProductSummaryDTO.builder()
                        .id(product.getId())
                        .sku(product.getSku())
                        .name(product.getName())
                        .unitPrice(product.getUnitPrice())
                        .build())
                .collect(Collectors.toList());
    }
}