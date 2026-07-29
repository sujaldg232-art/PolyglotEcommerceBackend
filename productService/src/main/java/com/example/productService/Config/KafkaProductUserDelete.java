package com.example.productService.Config;

import com.example.productService.entities.Product;
import com.example.productService.repo.ProductRepo;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        List<Product> list = productRepo.findBySellerId(userID);
        for(int i = 0; i < list.size();i++){
            list.get(i).setAvailable(false);
        }
    }
}