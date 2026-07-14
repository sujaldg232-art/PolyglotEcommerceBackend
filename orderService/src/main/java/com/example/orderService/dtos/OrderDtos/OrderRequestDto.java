package com.example.orderService.dtos.OrderDtos;

import com.example.orderService.dtos.OrderLineDtos.OrderLineRequestDto;

import java.util.List;
import java.util.UUID;

public record OrderRequestDto(
        UUID buyerId,
        List<OrderLineRequestDto> orderLines
){}