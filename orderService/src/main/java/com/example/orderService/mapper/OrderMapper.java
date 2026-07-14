package com.example.orderService.mapper;

import com.example.orderService.dtos.OrderDtos.OrderRequestDto;
import com.example.orderService.dtos.OrderDtos.OrderResponseDto;
import com.example.orderService.entities.OrderData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, uses = {OrderLineMapper.class})
public interface OrderMapper {

    OrderResponseDto entityToResponse(OrderData orderData);

    OrderData requestToEntity(OrderRequestDto orderRequestDto);
}