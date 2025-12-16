package com.portfolio.stocksage.dto.mapper;

import com.portfolio.stocksage.dto.response.InventoryDTO;
import com.portfolio.stocksage.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "product.id", source = "product.id")
    @Mapping(target = "product.sku", source = "product.sku")
    @Mapping(target = "product.name", source = "product.name")
    @Mapping(target = "product.unitPrice", source = "product.unitPrice")
    @Mapping(target = "product.category", source = "product.category.name")
    @Mapping(target = "warehouse.id", source = "warehouse.id")
    @Mapping(target = "warehouse.name", source = "warehouse.name")
    @Mapping(target = "warehouse.location", source = "warehouse.location")
    @Mapping(target = "totalValue", expression = "java(inventory.getProduct().getUnitPrice().multiply(new java.math.BigDecimal(inventory.getQuantity())))")
    InventoryDTO toDto(Inventory inventory);
}