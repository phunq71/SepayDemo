package com.example.sepay.dto;

import lombok.Data;

@Data
public class CallbackData {
    private String transId;
    private String orderId;
    private String status;
    private String message;
    private String checksum;
}
