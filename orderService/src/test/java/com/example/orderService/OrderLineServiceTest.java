package com.example.orderService;

import com.example.orderService.dtos.OrderLineDtos.OrderLineResponseDto;
import com.example.orderService.entities.OrderLine;
import com.example.orderService.mapper.OrderLineMapper;
import com.example.orderService.repos.OrderLineRepo;
import com.example.orderService.service.OrderLineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderLineServiceTest {

    @Mock
    private OrderLineRepo orderLineRepo;

    @Mock
    private OrderLineMapper orderLineMapper;

    @InjectMocks
    private OrderLineService orderLineService;

    @Test
    void findById() {
        Long id = 1L;
        OrderLine orderLine = new OrderLine();
        OrderLineResponseDto responseDto = mock(OrderLineResponseDto.class);

        when(orderLineRepo.findById(id)).thenReturn(Optional.of(orderLine));
        when(orderLineMapper.entityToResponse(orderLine)).thenReturn(responseDto);

        OrderLineResponseDto result = orderLineService.findById(id);

        assertEquals(responseDto, result);
    }
}
