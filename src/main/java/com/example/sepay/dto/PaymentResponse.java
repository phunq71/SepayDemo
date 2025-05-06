package com.example.sepay.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private String orderId;
    private String paymentUrl;
    private String qrCodeUrl; // Thêm trường mới
    private String status;
}
