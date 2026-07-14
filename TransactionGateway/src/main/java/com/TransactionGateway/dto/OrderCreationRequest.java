package com.TransactionGateway.dto;

public record OrderCreationRequest(
        int amount,
        String currency,
        String receiptId
) {
}
