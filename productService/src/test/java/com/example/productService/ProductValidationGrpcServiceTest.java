package com.example.productService;

import com.example.productService.service.ProductService;
import com.example.productService.service.ProductValidationGrpcService;
import io.grpc.stub.StreamObserver;
import org.example.grpc.ProductValidationDto;
import org.example.grpc.ProductValidationDtoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProductValidationGrpcServiceTest {

    @Mock
    private ProductService productService;

    @Mock
    private StreamObserver<ProductValidationDtoResponse> responseObserver;

    @InjectMocks
    private ProductValidationGrpcService productValidationGrpcService;

    @Captor
    private ArgumentCaptor<ProductValidationDtoResponse> responseCaptor;

    private ProductValidationDto productValidationDto;

    @BeforeEach
    void init() {
        productValidationDto = ProductValidationDto.newBuilder()
                .setProductID(UUID.randomUUID().toString())
                .setQuantity(1)
                .build();
    }

    @Test
    void validateForOrderServiceSuccess() {
        UUID productId = UUID.fromString(productValidationDto.getProductID());
        int quantity = productValidationDto.getQuantity();

        Mockito.when(productService.isValidForOrder(productId, quantity)).thenReturn(true);

        productValidationGrpcService.validateProductValidation(productValidationDto, responseObserver);

        verify(productService).isValidForOrder(productId, quantity);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ProductValidationDtoResponse response = responseCaptor.getValue();
        assertTrue(response.getIsValid());
        assertEquals("", response.getError());
    }

    @Test
    void validateForOrderServiceFalseReturn(){
        UUID productId = UUID.fromString(productValidationDto.getProductID());
        int quantity = productValidationDto.getQuantity();

        Mockito.when(productService.isValidForOrder(productId, quantity)).thenReturn(false);

        productValidationGrpcService.validateProductValidation(productValidationDto, responseObserver);

        verify(productService).isValidForOrder(productId, quantity);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ProductValidationDtoResponse response = responseCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertFalse(response.getIsValid());
        assertEquals("Product validation failed or insufficient stock", response.getError());
    }
}