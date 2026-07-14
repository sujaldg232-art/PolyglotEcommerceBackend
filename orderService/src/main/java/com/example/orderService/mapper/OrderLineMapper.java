package com.example.orderService.mapper;

import com.example.orderService.dtos.OrderLineDtos.OrderLineRequestDto;
import com.example.orderService.dtos.OrderLineDtos.OrderLineResponseDto;
import com.example.orderService.entities.OrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface OrderLineMapper {

    OrderLineRequestDto entityToRequest(OrderLine orderLine);

    OrderLine requestToEntity(OrderLineRequestDto orderLineRequestDto);

    @Mapping(source = "orderLineID", target = "orderLineId")
    OrderLineResponseDto entityToResponse(OrderLine orderLine);
}