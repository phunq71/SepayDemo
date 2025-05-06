package com.example.sepay.controller;

import com.example.sepay.dto.CallbackData;
import com.example.sepay.dto.PaymentRequest;
import com.example.sepay.dto.PaymentResponse;
import com.example.sepay.service.SePayService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    
    private final SePayService sePayService;
    
    public PaymentController(SePayService sePayService) {
        this.sePayService = sePayService;
    }
    
    @PostMapping("/create")
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        return sePayService.createPayment(request);
    }
    
    @PostMapping("/callback")
    public String handleCallback(@RequestBody CallbackData callbackData) {
        sePayService.handleCallback(callbackData);
        return "OK";
    }
}