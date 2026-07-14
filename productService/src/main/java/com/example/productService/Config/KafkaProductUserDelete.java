package com.example.productService.Config;

import com.example.productService.repo.ProductRepo;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class KafkaProductUserDelete {

    private final ProductRepo productRepo;

    public KafkaProductUserDelete(ProductRepo productRepo){
        this.productRepo = productRepo;
    }

    @KafkaListener(topics = "userDeletionTopic", groupId = "1")
    @Transactional
    public void userDeletionEventListener(String data){
        UUID userID = UUID.fromString(data);
        productRepo.deleteBySellerId(userID);
    }
}