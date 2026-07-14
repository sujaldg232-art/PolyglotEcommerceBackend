package com.example.orderService.dtos.OrderLineDtos;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record OrderLineRequestDto (
        UUID sellerID,
        Integer quantity,
        UUID productID,
        BigDecimal totalPrice
){}
