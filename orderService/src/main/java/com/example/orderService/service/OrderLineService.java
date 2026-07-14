package com.example.orderService.service;

import com.example.orderService.dtos.OrderLineDtos.OrderLineRequestDto;
import com.example.orderService.dtos.OrderLineDtos.OrderLineResponseDto;
import com.example.orderService.entities.OrderLine;
import com.example.orderService.mapper.OrderLineMapper;
import com.example.orderService.repos.OrderLineRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderLineService {

    OrderLineRepo orderLineRepo;
    OrderLineMapper orderLineMapper;

    @Autowired
    public OrderLineService(OrderLineRepo orderLineRepo,OrderLineMapper orderLineMapper){
        this.orderLineRepo = orderLineRepo;
        this.orderLineMapper = orderLineMapper;
    }


    public OrderLineResponseDto findById(Long id){
        return orderLineMapper.entityToResponse(orderLineRepo.findById(id).orElse(null));
    }
}
