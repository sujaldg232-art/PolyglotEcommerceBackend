package com.example.productService.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.grpc.ProductValidationDto;
import org.example.grpc.ProductValidationDtoResponse;
import org.example.grpc.productValidateDtoGrpc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@GrpcService
public class ProductValidationGrpcService extends productValidateDtoGrpc.productValidateDtoImplBase {

    private final ProductService productService;

    @Autowired
    public ProductValidationGrpcService(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void validateProductValidation(ProductValidationDto request, StreamObserver<ProductValidationDtoResponse> responseObserver) {
        UUID productId = UUID.fromString(request.getProductID());
        int quantity = request.getQuantity();

        Boolean isValid = productService.isValidForOrder(productId, quantity);

        ProductValidationDtoResponse response = ProductValidationDtoResponse.newBuilder()
                .setIsValid(isValid)
                .setError(isValid ? "" : "Product validation failed or insufficient stock")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


}
