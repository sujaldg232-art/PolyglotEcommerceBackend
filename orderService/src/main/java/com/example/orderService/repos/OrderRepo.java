package com.example.orderService.repos;

import com.example.orderService.entities.OrderData;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepo extends JpaRepository<OrderData, UUID> {


    Optional<OrderData> findByBuyerId(UUID uuid);
}
