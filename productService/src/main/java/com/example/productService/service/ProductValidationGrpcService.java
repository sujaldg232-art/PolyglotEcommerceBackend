package com.example.productService.service;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.grpc.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService
public class ProductValidationGrpcService extends OrderLineValidationServiceGrpc.OrderLineValidationServiceImplBase {

    private final ProductService productService;

    @Autowired
    public ProductValidationGrpcService(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void validateOrderLine(OrderlineValidationDto request, StreamObserver<OrderLineValidationOutPut> responseObserver) {
        OrderLineValidationOutPut internalResponse = productService.isValid(request);
        responseObserver.onNext(internalResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void validateMultipleOrderLines(BatchOrderLineValidationRequest request, StreamObserver<BatchOrderLineValidationResponse> responseObserver) {
        List<OrderlineValidationDto> list = request.getRequestsList();
        List<OrderLineValidationOutPut> validationOutputs = productService.isValidBatch(list);
        BatchOrderLineValidationResponse response = BatchOrderLineValidationResponse.newBuilder()
                .addAllResponses(validationOutputs)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}