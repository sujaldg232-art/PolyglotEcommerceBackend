package com.example.orderService.controller;

import com.example.orderService.dtos.OrderLineDtos.OrderLineRequestDto;
import com.example.orderService.dtos.OrderDtos.OrderResponseDto;
import com.example.orderService.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/order")
public class OrderController {

    OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping("/addOneOrderLine")
    public ResponseEntity<OrderResponseDto> addOrderLine(
            @RequestHeader("X-User-Id") String userIdStr
            , @RequestBody OrderLineRequestDto orderLineRequestDto){
        UUID userId = UUID.fromString(userIdStr);

        OrderResponseDto orderResponseDto = orderService.addOrderLine(userId,orderLineRequestDto);

        if(orderResponseDto == null){
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponseDto);
    }

    @PostMapping("/addMultipleOrderLine")
    public ResponseEntity<OrderResponseDto> addOrderLine(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestBody List<OrderLineRequestDto> orderLineRequestDto) {

        UUID userId = UUID.fromString(userIdStr);
        OrderResponseDto orderResponseDto = orderService.addMultipleOrderLine(userId, orderLineRequestDto);

        if (orderResponseDto == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponseDto);
    }

    @PostMapping("/removeOneOrderLine")
    public ResponseEntity<OrderResponseDto> removeOrderLine(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestParam Long orderLineid) {

        UUID userId = UUID.fromString(userIdStr);
        OrderResponseDto orderResponseDto = orderService.deleteOrderLine(userId, orderLineid);

        if (orderResponseDto == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponseDto);
    }

    @PostMapping("/removeMultipleOrderLines")
    public ResponseEntity<OrderResponseDto> removeMultipleOrderLines(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestParam List<Long> orderLineid) {

        UUID userId = UUID.fromString(userIdStr);
        OrderResponseDto response = orderService.deleteMultipleOrderLine(userId, orderLineid);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }
}
