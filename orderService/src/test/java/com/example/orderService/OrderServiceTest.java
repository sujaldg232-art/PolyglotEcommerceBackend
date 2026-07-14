package com.example.orderService;

import com.example.orderService.dtos.OrderLineDtos.OrderLineRequestDto;
import com.example.orderService.dtos.OrderDtos.OrderResponseDto;
import com.example.orderService.entities.OrderData;
import com.example.orderService.entities.OrderLine;
import com.example.orderService.mapper.OrderLineMapper;
import com.example.orderService.mapper.OrderMapper;
import com.example.orderService.repos.OrderRepo;
import com.example.orderService.service.OrderService;
import com.example.orderService.service.ProductToOrderServiceGrpc;
import org.example.grpc.ProductValidationDtoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderLineMapper orderLineMapper;

    @Mock
    private ProductToOrderServiceGrpc productToOrderServiceGrpc;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createEmptyOrder() {
        UUID buyerId = UUID.randomUUID();
        OrderData orderData = OrderData.builder()
                .buyerId(buyerId)
                .orderLines(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .build();
        OrderResponseDto responseDto = mock(OrderResponseDto.class);

        when(orderRepo.save(any(OrderData.class))).thenReturn(orderData);
        when(orderMapper.entityToResponse(orderData)).thenReturn(responseDto);

        OrderResponseDto result = orderService.createEmptyOrder(buyerId);

        assertEquals(responseDto, result);
    }

    @Test
    void validateOrderLines() {
        OrderLineRequestDto validDto = mock(OrderLineRequestDto.class);
        OrderLineRequestDto invalidDto = mock(OrderLineRequestDto.class);
        List<OrderLineRequestDto> dtoList = new ArrayList<>(Arrays.asList(validDto, invalidDto));

        ProductValidationDtoResponse validResponse = ProductValidationDtoResponse.newBuilder().setIsValid(true).build();
        ProductValidationDtoResponse invalidResponse = ProductValidationDtoResponse.newBuilder().setIsValid(false).build();

        when(productToOrderServiceGrpc.validateOrderLine(validDto)).thenReturn(validResponse);
        when(productToOrderServiceGrpc.validateOrderLine(invalidDto)).thenReturn(invalidResponse);

        List<OrderLineRequestDto> result = orderService.validateOrderLines(dtoList);

        assertEquals(1, result.size());
        assertEquals(validDto, result.getFirst());
    }

    @Test
    void deleteOrderLine() {
        UUID buyerId = UUID.randomUUID();
        Long orderLineId = 1L;

        OrderLine line = new OrderLine();
        line.setOrderLineID(orderLineId);
        line.setTotalPrice(BigDecimal.TEN);

        List<OrderLine> lines = new ArrayList<>();
        lines.add(line);

        OrderData orderData = new OrderData();
        orderData.setBuyerId(buyerId);
        orderData.setOrderLines(lines);
        orderData.setTotalPrice(BigDecimal.TEN);

        OrderResponseDto responseDto = mock(OrderResponseDto.class);

        when(orderRepo.findByBuyerId(buyerId)).thenReturn(Optional.of(orderData));
        when(orderRepo.save(any(OrderData.class))).thenReturn(orderData);
        when(orderMapper.entityToResponse(orderData)).thenReturn(responseDto);

        OrderResponseDto result = orderService.deleteOrderLine(buyerId, orderLineId);

        assertEquals(responseDto, result);
        assertEquals(BigDecimal.ZERO, orderData.getTotalPrice());
        assertTrue(orderData.getOrderLines().isEmpty());
    }

    @Test
    void deleteMultipleOrderLine() {
        UUID buyerId = UUID.randomUUID();
        Long orderLineId1 = 1L;
        Long orderLineId2 = 2L;

        OrderLine line1 = new OrderLine();
        line1.setOrderLineID(orderLineId1);
        line1.setTotalPrice(BigDecimal.TEN);

        OrderLine line2 = new OrderLine();
        line2.setOrderLineID(orderLineId2);
        line2.setTotalPrice(BigDecimal.valueOf(15));

        List<OrderLine> lines = new ArrayList<>(Arrays.asList(line1, line2));

        OrderData orderData = new OrderData();
        orderData.setBuyerId(buyerId);
        orderData.setOrderLines(lines);
        orderData.setTotalPrice(BigDecimal.valueOf(25));

        OrderResponseDto responseDto = mock(OrderResponseDto.class);

        when(orderRepo.findByBuyerId(buyerId)).thenReturn(Optional.of(orderData));
        when(orderRepo.save(any(OrderData.class))).thenReturn(orderData);
        when(orderMapper.entityToResponse(orderData)).thenReturn(responseDto);

        OrderResponseDto result = orderService.deleteMultipleOrderLine(buyerId, Arrays.asList(orderLineId1, orderLineId2));

        assertEquals(responseDto, result);
        assertEquals(BigDecimal.ZERO, orderData.getTotalPrice());
        assertTrue(orderData.getOrderLines().isEmpty());
    }

    @Test
    void addOrderLine() {
        UUID buyerId = UUID.randomUUID();
        OrderLineRequestDto requestDto = mock(OrderLineRequestDto.class);

        OrderData orderData = new OrderData();
        orderData.setBuyerId(buyerId);
        orderData.setOrderLines(new ArrayList<>());
        orderData.setTotalPrice(BigDecimal.ZERO);

        OrderLine newOrderLine = new OrderLine();
        newOrderLine.setTotalPrice(BigDecimal.TEN);

        ProductValidationDtoResponse validResponse = ProductValidationDtoResponse.newBuilder().setIsValid(true).build();
        OrderResponseDto responseDto = mock(OrderResponseDto.class);

        when(orderRepo.findByBuyerId(buyerId)).thenReturn(Optional.of(orderData));
        when(productToOrderServiceGrpc.validateOrderLine(requestDto)).thenReturn(validResponse);
        when(orderLineMapper.requestToEntity(requestDto)).thenReturn(newOrderLine);
        when(orderRepo.save(any(OrderData.class))).thenReturn(orderData);
        when(orderMapper.entityToResponse(orderData)).thenReturn(responseDto);

        OrderResponseDto result = orderService.addOrderLine(buyerId, requestDto);

        assertEquals(responseDto, result);
        assertEquals(BigDecimal.TEN, orderData.getTotalPrice());
        assertEquals(1, orderData.getOrderLines().size());
    }

    @Test
    void addMultipleOrderLine() {
        UUID buyerId = UUID.randomUUID();
        OrderLineRequestDto requestDto = mock(OrderLineRequestDto.class);
        List<OrderLineRequestDto> requestDtos = Collections.singletonList(requestDto);

        OrderData orderData = new OrderData();
        orderData.setBuyerId(buyerId);
        orderData.setOrderLines(new ArrayList<>());
        orderData.setTotalPrice(BigDecimal.ZERO);

        OrderLine newOrderLine = new OrderLine();
        newOrderLine.setTotalPrice(BigDecimal.TEN);

        ProductValidationDtoResponse validResponse = ProductValidationDtoResponse.newBuilder().setIsValid(true).build();
        OrderResponseDto responseDto = mock(OrderResponseDto.class);

        when(orderRepo.findByBuyerId(buyerId)).thenReturn(Optional.of(orderData));
        when(productToOrderServiceGrpc.validateOrderLine(requestDto)).thenReturn(validResponse);
        when(orderLineMapper.requestToEntity(requestDto)).thenReturn(newOrderLine);
        when(orderRepo.save(any(OrderData.class))).thenReturn(orderData);
        when(orderMapper.entityToResponse(orderData)).thenReturn(responseDto);

        OrderResponseDto result = orderService.addMultipleOrderLine(buyerId, requestDtos);

        assertEquals(responseDto, result);
        assertEquals(BigDecimal.TEN, orderData.getTotalPrice());
        assertEquals(1, orderData.getOrderLines().size());
    }

    @Test
    void delete() {
        UUID id = UUID.randomUUID();
        OrderData orderData = new OrderData();

        when(orderRepo.findById(id)).thenReturn(Optional.of(orderData));

        int result = orderService.delete(id);

        assertEquals(0, result);
        verify(orderRepo).deleteById(id);
    }

    @Test
    void deleteByBuyerId() {
        UUID buyerId = UUID.randomUUID();
        OrderData orderData = new OrderData();

        when(orderRepo.findByBuyerId(buyerId)).thenReturn(Optional.of(orderData));

        orderService.deleteByBuyerId(buyerId);

        verify(orderRepo).delete(orderData);
    }
}
