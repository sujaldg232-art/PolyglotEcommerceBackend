package com.example.orderService.service;

import com.example.orderService.dtos.OrderLineDtos.OrderLineRequestDto;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.grpc.ProductValidationDto;
import org.example.grpc.ProductValidationDtoResponse;
import org.example.grpc.productValidateDtoGrpc;
import org.springframework.stereotype.Service;

@Service
public class ProductToOrderServiceGrpc {

    @GrpcClient("productService")
    private productValidateDtoGrpc.productValidateDtoBlockingStub userBlockingStub;

    public ProductValidationDtoResponse validateOrderLine(OrderLineRequestDto orderLine) {
        ProductValidationDto productValidationDto = ProductValidationDto.newBuilder()
                .setProductID(String.valueOf(orderLine.productID()))
                .setQuantity(orderLine.quantity())
                .build();
        return userBlockingStub.validateProductValidation(productValidationDto);
    }
}