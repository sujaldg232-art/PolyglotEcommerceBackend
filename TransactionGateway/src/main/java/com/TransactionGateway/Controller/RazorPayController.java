package com.TransactionGateway.Controller;

import com.TransactionGateway.Service.RazorPayService;
import com.TransactionGateway.dto.OrderCreationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Payment ")
public class RazorPayController {

    RazorPayService razorPayService;

    @Autowired
    public RazorPayController(RazorPayService razorPayService){
        this.razorPayService = razorPayService;
    }

    @PostMapping("/create-order")
    public String createOrder(
            @RequestBody OrderCreationRequest orderCreationRequest
    ){
       try {
           return razorPayService.createOrder(orderCreationRequest.amount(), orderCreationRequest.currency(), orderCreationRequest.receiptId());
       }catch (Exception e){
           throw new RuntimeException(e);
       }
    }

}
