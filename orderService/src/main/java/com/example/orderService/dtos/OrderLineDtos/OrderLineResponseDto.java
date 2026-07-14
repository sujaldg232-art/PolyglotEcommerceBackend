package com.example.orderService.dtos.OrderLineDtos;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OrderLineResponseDto(
        Long orderLineId,
         UUID sellerID,
         UUID productID,
         Integer quantity,
         BigDecimal totalPrice
) {}
