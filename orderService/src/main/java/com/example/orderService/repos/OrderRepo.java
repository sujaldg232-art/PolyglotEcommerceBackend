package com.example.orderService.repos;

import com.example.orderService.entities.OrderData;

import com.example.orderService.entities.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepo extends JpaRepository<OrderData, UUID> {

    @Query("select p.orderLines from OrderData p where p = :orderData")
    List<OrderLine> fetchAllTheOrderLine(OrderData orderData);

    Optional<OrderData> findByBuyerId(UUID uuid);
}
