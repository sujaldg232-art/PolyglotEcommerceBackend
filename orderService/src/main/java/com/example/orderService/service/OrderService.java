package com.example.orderService.service;

import com.example.orderService.dtos.OrderLineDtos.OrderLineRequestDto;
import com.example.orderService.dtos.OrderDtos.OrderResponseDto;
import com.example.orderService.entities.OrderData;
import com.example.orderService.entities.OrderLine;
import com.example.orderService.mapper.OrderLineMapper;
import com.example.orderService.mapper.OrderMapper;
import com.example.orderService.repos.OrderRepo;
import jakarta.transaction.TransactionScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.grpc.ProductValidationDtoResponse;

import java.math.BigDecimal;
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
                .build();

        return orderMapper.entityToResponse(orderRepo.save(orderData));
    }

    @Transactional(readOnly = true)
    public List<OrderLineRequestDto> validateOrderLines(List<OrderLineRequestDto> orderLines){
        List<OrderLineRequestDto> validOrderLineList = new ArrayList<>(orderLines);
        validOrderLineList.removeIf(orderLine -> {

            ProductValidationDtoResponse productValidationDto = productToOrderServiceGrpc.validateOrderLine(orderLine);
            return !productValidationDto.getIsValid();
        });
        return validOrderLineList;
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

        for (OrderLine line : orderLines) {
            if (idsToDelete.contains(line.getOrderLineID())) {
                totalToBeRemoved = totalToBeRemoved.add(line.getTotalPrice());
            }
        }

        orderData.setTotalPrice(orderData.getTotalPrice().subtract(totalToBeRemoved));
        orderLines.removeIf(line -> idsToDelete.contains(line.getOrderLineID()));

        return orderMapper.entityToResponse(orderRepo.save(orderData));
    }

    @Transactional
    public OrderResponseDto addOrderLine(UUID buyerId, OrderLineRequestDto orderLineRequestDto) {
        OrderData orderData = orderRepo.findByBuyerId(buyerId).orElse(null);
        if (orderData == null) return null;

        ProductValidationDtoResponse validation = productToOrderServiceGrpc.validateOrderLine(orderLineRequestDto);
        if (!validation.getIsValid()) {
            return null;
        }

        OrderLine newOrderLine = orderLineMapper.requestToEntity(orderLineRequestDto);
        orderData.getOrderLines().add(newOrderLine);

        BigDecimal newPrice = orderData.getTotalPrice().add(newOrderLine.getTotalPrice());
        orderData.setTotalPrice(newPrice);

        return orderMapper.entityToResponse(orderRepo.save(orderData));
    }

    @Transactional
    public OrderResponseDto addMultipleOrderLine(UUID buyerId, List<OrderLineRequestDto> orderLineRequestDtos) {
        OrderData orderData = orderRepo.findByBuyerId(buyerId).orElse(null);
        if (orderData == null) return null;

        List<OrderLineRequestDto> validOrderLines = validateOrderLines(orderLineRequestDtos);

        List<OrderLine> listOfAllOL = orderData.getOrderLines();
        BigDecimal totalToBeAdded = BigDecimal.ZERO;

        for (OrderLineRequestDto dto : validOrderLines) {
            OrderLine newOrderLine = orderLineMapper.requestToEntity(dto);
            if (newOrderLine == null) continue;

            listOfAllOL.add(newOrderLine);
            totalToBeAdded = totalToBeAdded.add(newOrderLine.getTotalPrice());
        }

        orderData.setTotalPrice(orderData.getTotalPrice().add(totalToBeAdded));
        return orderMapper.entityToResponse(orderRepo.save(orderData));
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



    @TransactionScoped
    public void deleteByBuyerId(UUID buyerId){
        OrderData orderData = orderRepo.findByBuyerId(buyerId).orElse(null);

        if(orderData != null){
            orderRepo.delete(orderData);
        }


    }
}