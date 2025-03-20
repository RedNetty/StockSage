package com.portfolio.stocksage.dto.mapper;

import com.portfolio.stocksage.dto.request.WarehouseCreateDTO;
import com.portfolio.stocksage.dto.response.WarehouseDTO;
import com.portfolio.stocksage.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Warehouse toEntity(WarehouseCreateDTO dto);

    @Mapping(target = "productCount", ignore = true)
    @Mapping(target = "totalItems", ignore = true)
    WarehouseDTO toDto(Warehouse entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "inventories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(WarehouseCreateDTO dto, @MappingTarget Warehouse entity);
}