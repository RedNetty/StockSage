package com.portfolio.stocksage.dto.mapper;

import com.portfolio.stocksage.dto.request.TransactionCreateDTO;
import com.portfolio.stocksage.dto.response.ProductSummaryDTO;
import com.portfolio.stocksage.dto.response.TransactionDTO;
import com.portfolio.stocksage.dto.response.UserSummaryDTO;
import com.portfolio.stocksage.dto.response.WarehouseSummaryDTO;
import com.portfolio.stocksage.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "sourceWarehouse", ignore = true)
    @Mapping(target = "destinationWarehouse", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Transaction toEntity(TransactionCreateDTO dto);

    @Mapping(target = "product.id", source = "product.id")
    @Mapping(target = "product.sku", source = "product.sku")
    @Mapping(target = "product.name", source = "product.name")
    @Mapping(target = "product.unitPrice", source = "product.unitPrice")
    @Mapping(target = "warehouse.id", source = "warehouse.id")
    @Mapping(target = "warehouse.name", source = "warehouse.name")
    @Mapping(target = "warehouse.location", source = "warehouse.location")
    @Mapping(target = "sourceWarehouse.id", source = "sourceWarehouse.id")
    @Mapping(target = "sourceWarehouse.name", source = "sourceWarehouse.name")
    @Mapping(target = "sourceWarehouse.location", source = "sourceWarehouse.location")
    @Mapping(target = "destinationWarehouse.id", source = "destinationWarehouse.id")
    @Mapping(target = "destinationWarehouse.name", source = "destinationWarehouse.name")
    @Mapping(target = "destinationWarehouse.location", source = "destinationWarehouse.location")
    @Mapping(target = "createdBy.id", source = "createdBy.id")
    @Mapping(target = "createdBy.username", source = "createdBy.username")
    @Mapping(target = "createdBy.fullName", expression = "java(transaction.getCreatedBy().getFirstName() + \" \" + transaction.getCreatedBy().getLastName())")
    @Mapping(target = "totalAmount", expression = "java(transaction.getUnitPrice().multiply(new java.math.BigDecimal(transaction.getQuantity())))")
    TransactionDTO toDto(Transaction transaction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "sourceWarehouse", ignore = true)
    @Mapping(target = "destinationWarehouse", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(TransactionCreateDTO dto, @MappingTarget Transaction entity);

    default ProductSummaryDTO map(com.portfolio.stocksage.entity.Product product) {
        if (product == null) {
            return null;
        }
        return ProductSummaryDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .unitPrice(product.getUnitPrice())
                .build();
    }

    default WarehouseSummaryDTO map(com.portfolio.stocksage.entity.Warehouse warehouse) {
        if (warehouse == null) {
            return null;
        }
        return WarehouseSummaryDTO.builder()
                .id(warehouse.getId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .build();
    }

    default UserSummaryDTO map(com.portfolio.stocksage.entity.User user) {
        if (user == null) {
            return null;
        }
        return UserSummaryDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .build();
    }
}