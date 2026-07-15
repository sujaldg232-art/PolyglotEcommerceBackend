package com.example.orderService.repos;

import com.example.orderService.entities.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLineRepo extends JpaRepository<OrderLine,Long> {


}
