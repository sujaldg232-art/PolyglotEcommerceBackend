package com.example.orderService;

import com.example.orderService.dtos.OrderLineDtos.OrderLineRequestDto;
import com.example.orderService.service.ProductToOrderServiceGrpc;
import org.example.grpc.ProductValidationDto;
import org.example.grpc.ProductValidationDtoResponse;
import org.example.grpc.productValidateDtoGrpc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductToOrderServiceGrpcTest {

    @Mock
    private productValidateDtoGrpc.productValidateDtoBlockingStub userBlockingStub;

    @InjectMocks
    private ProductToOrderServiceGrpc productToOrderServiceGrpc;

    @Test
    void validateOrderLine() {
        OrderLineRequestDto requestDto = mock(OrderLineRequestDto.class);
        when(requestDto.productID()).thenReturn(UUID.randomUUID());
        when(requestDto.quantity()).thenReturn(5);

        ProductValidationDtoResponse expectedResponse = ProductValidationDtoResponse.newBuilder()
                .setIsValid(true)
                .build();

        when(userBlockingStub.validateProductValidation(any(ProductValidationDto.class)))
                .thenReturn(expectedResponse);

        ProductValidationDtoResponse actualResponse = productToOrderServiceGrpc.validateOrderLine(requestDto);

        assertEquals(expectedResponse, actualResponse);
    }
}
