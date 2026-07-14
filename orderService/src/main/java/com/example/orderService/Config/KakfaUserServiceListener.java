package com.example.orderService.Config;

import com.example.orderService.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Configuration
@EnableKafka
public class KakfaUserServiceListener {

    OrderService orderService;

    @Autowired
    public KakfaUserServiceListener(OrderService orderService){
        this.orderService = orderService;
    }


    @KafkaListener(topics = "userCreationTopic", groupId = "2")
    @Transactional
    public void userCreationEventListener(String data){
        UUID id = UUID.fromString(data);
        orderService.createEmptyOrder(id);
    }

    @KafkaListener(topics = "userDeletionTopic", groupId = "2")
    @Transactional
    public void userDeletionEventListener(String data){
        UUID userID = UUID.fromString(data);
        orderService.deleteByBuyerId(userID);
    }
}
