package com.example.sepay.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private BigDecimal amount;
    private String description;
    private String returnUrl;
    private String cancelUrl;
}
