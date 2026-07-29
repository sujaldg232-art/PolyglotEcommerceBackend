
package com.example.orderService.dtos.OrderDtos;

import com.example.orderService.dtos.OrderLineDtos.OrderLineResponseDto;
import com.example.orderService.entities.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderResponseDto(
        UUID buyerId,
        List<OrderLineResponseDto> orderLines,
        BigDecimal totalPrice,
        OrderStatus orderStatus
) {
}