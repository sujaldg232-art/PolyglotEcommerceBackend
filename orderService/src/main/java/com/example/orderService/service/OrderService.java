package com.example.orderService.service;

import com.example.orderService.dtos.OrderLineDtos.OrderLineRequestDto;
import com.example.orderService.dtos.OrderDtos.OrderResponseDto;
import com.example.orderService.entities.OrderData;
import com.example.orderService.entities.OrderLine;
import com.example.orderService.entities.OrderStatus;
import com.example.orderService.mapper.OrderLineMapper;
import com.example.orderService.mapper.OrderMapper;
import com.example.orderService.repos.OrderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepo orderRepo;
    private final OrderMapper orderMapper;
    private final OrderLineMapper orderLineMapper;
    private final ProductToOrderServiceGrpc productToOrderServiceGrpc;

    @Autowired
    public OrderService(OrderRepo orderRepo, OrderMapper orderMapper, OrderLineMapper orderLineMapper, ProductToOrderServiceGrpc productToOrderServiceGrpc){
        this.orderMapper = orderMapper;
        this.orderRepo = orderRepo;
        this.orderLineMapper = orderLineMapper;
        this.productToOrderServiceGrpc = productToOrderServiceGrpc;
    }

    @Transactional
    public OrderResponseDto createEmptyOrder(UUID buyerId){
        List<OrderLine> list = new ArrayList<>();

        OrderData orderData = OrderData.builder()
                .buyerId(buyerId)
                .orderLines(list)
                .totalPrice(BigDecimal.ZERO)
                .orderStatus(OrderStatus.PENDING)
                .build();

        return orderMapper.entityToResponse(orderRepo.save(orderData));
    }

    @Transactional
    public OrderResponseDto deleteOrderLine(UUID buyerId, Long orderLineId){
        OrderData orderData = orderRepo.findByBuyerId(buyerId).orElse(null);
        if (orderData == null) return null;

        List<OrderLine> orderLines = orderData.getOrderLines();
        BigDecimal toBeRemoved = BigDecimal.ZERO;

        for (OrderLine line : orderLines) {
            if (line.getOrderLineID().equals(orderLineId)) {
                toBeRemoved = line.getTotalPrice();
                break;
            }
        }

        orderData.setTotalPrice(orderData.getTotalPrice().subtract(toBeRemoved));
        orderLines.removeIf(line -> line.getOrderLineID().equals(orderLineId));

        return orderMapper.entityToResponse(orderRepo.save(orderData));
    }

    @Transactional
    public OrderResponseDto deleteMultipleOrderLine(UUID buyerId, List<Long> orderLineID){
        OrderData orderData = orderRepo.findByBuyerId(buyerId).orElse(null);
        if (orderData == null) return null;

        List<OrderLine> orderLines = orderData.getOrderLines();
        Set<Long> idsToDelete = new HashSet<>(orderLineID);
        BigDecimal totalToBeRemoved = BigDecimal.ZERO;

        Iterator<OrderLine> iterator = orderLines.iterator();
        while (iterator.hasNext()) {
            OrderLine line = iterator.next();
            if (idsToDelete.contains(line.getOrderLineID())) {
                totalToBeRemoved = totalToBeRemoved.add(line.getTotalPrice());
                iterator.remove();
            }
        }

        orderData.setTotalPrice(orderData.getTotalPrice().subtract(totalToBeRemoved));
        return orderMapper.entityToResponse(orderRepo.save(orderData));
    }

    @Transactional
    public OrderResponseDto addOrderLine(UUID buyerID,OrderLineRequestDto orderLineRequestDto) {
        OrderData orderData = orderRepo.findByBuyerId(buyerID).orElse(null);
        OrderLine orderLine = orderLineMapper.requestToEntity(orderLineRequestDto);

        org.example.grpc.OrderLineValidationOutPut validationOutput = productToOrderServiceGrpc.validateOrderLine(orderLineRequestDto);

        String error = validationOutput.getError();

        if(!error.equals("")){
            throw new ResponseStatusException(HttpStatusCode.valueOf(validationOutput.getErrorId()),error);
        }

        UUID sellerID = UUID.fromString(validationOutput.getSellerID());

        BigDecimal onePiecePrice = new BigDecimal(validationOutput.getPrice())  ;
        BigDecimal quantity = new BigDecimal(orderLineRequestDto.quantity());

        BigDecimal finalPrice = onePiecePrice.multiply(quantity);

        orderLine.setSellerID(UUID.fromString(validationOutput.getSellerID()));
        orderLine.setTotalPrice(finalPrice);
        orderData.setOrderStatus(OrderStatus.PENDING);
        
        List<OrderLine> list = orderData.getOrderLines();
        list.add(orderLine);

        orderData.setOrderLines(list);
        OrderResponseDto resp = orderMapper.entityToResponse(orderData);
        return resp;
    }

    @Transactional

    public OrderResponseDto addMultipleOrderLine(UUID buyerId, List<OrderLineRequestDto> orderLineRequestDtos) {
        OrderData orderData = orderRepo.findByBuyerId(buyerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        orderData.setOrderStatus(OrderStatus.PENDING);

        List<org.example.grpc.OrderLineValidationOutPut> validationOutputs = productToOrderServiceGrpc.validateOrderlines(orderLineRequestDtos);

        List<OrderLine> list = orderData.getOrderLines();

        for (int i = 0; i < orderLineRequestDtos.size(); i++) {
            OrderLineRequestDto requestDto = orderLineRequestDtos.get(i);
            org.example.grpc.OrderLineValidationOutPut validationOutput = validationOutputs.get(i);

            String error = validationOutput.getError();
            if (!error.isEmpty()) {
                throw new ResponseStatusException(org.springframework.http.HttpStatusCode.valueOf(validationOutput.getErrorId()), error);
            }

            OrderLine orderLine = orderLineMapper.requestToEntity(requestDto);

            BigDecimal onePiecePrice = new BigDecimal(validationOutput.getPrice());
            BigDecimal quantity = new BigDecimal(requestDto.quantity());
            orderLine.setTotalPrice(onePiecePrice.multiply(quantity));
            orderLine.setSellerID(UUID.fromString(validationOutput.getSellerID()));

            list.add(orderLine);
        }

        return orderMapper.entityToResponse(orderData);
    }

    @Transactional
    public int delete(UUID uuid){
        OrderData orderData = orderRepo.findById(uuid).orElse(null);
        if (orderData == null) {
            return 1;
        }
        orderRepo.deleteById(uuid);
        return 0;
    }

    @Transactional
    public void deleteByBuyerId(UUID buyerId){
        OrderData orderData = orderRepo.findByBuyerId(buyerId).orElse(null);
        if(orderData != null){
            orderRepo.delete(orderData);
        }
    }
}