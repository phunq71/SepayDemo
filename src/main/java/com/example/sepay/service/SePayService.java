package com.example.sepay.service;

import com.example.sepay.dto.CallbackData;
import com.example.sepay.dto.PaymentRequest;
import com.example.sepay.dto.PaymentResponse;
import com.example.sepay.entity.Transaction;
import com.example.sepay.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.math.BigDecimal;

@Service
public class SePayService {
    
    @Value("${sepay.api.key}")
    private String apiKey;
    
    @Value("${sepay.secret.key}")
    private String secretKey;
    
    @Value("${sepay.merchant.code}")
    private String merchantCode;
    
    @Value("${sepay.endpoint}")
    private String endpoint;
    
    private final TransactionRepository transactionRepository;
    
    public SePayService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    
    public PaymentResponse createPayment(PaymentRequest request) {
        String orderId = generateOrderId();
        
        Transaction transaction = new Transaction();
        transaction.setOrderId(orderId);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setStatus("PENDING");
        transactionRepository.save(transaction);
        
        String paymentUrl = buildPaymentUrl(request, orderId);
        
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(orderId);
        response.setPaymentUrl(paymentUrl);
        response.setStatus("PENDING");
        
        return response;
    }
    
    public void handleCallback(CallbackData callbackData) {
        // Verify checksum
        if (!verifyChecksum(callbackData)) {
            throw new RuntimeException("Invalid checksum");
        }
        
        Transaction transaction = transactionRepository.findByOrderId(callbackData.getOrderId());
        if (transaction != null) {
            transaction.setStatus(callbackData.getStatus());
            transaction.setSepayTransId(callbackData.getTransId());
            transactionRepository.save(transaction);
        }
    }
    
    private String generateOrderId() {
        return "SEPAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 5);
    }
    
    private String buildPaymentUrl(PaymentRequest request, String orderId) {
        return endpoint + "?" +
               "merchant_code=" + merchantCode +
               "&order_id=" + orderId +
               "&amount=" + request.getAmount() +
               "&description=" + encodeValue(request.getDescription()) +
               "&return_url=" + encodeValue(request.getReturnUrl()) +
               "&cancel_url=" + encodeValue(request.getCancelUrl()) +
               "&signature=" + generateSignature(orderId, request.getAmount());
    }
    
    private String generateSignature(String orderId, BigDecimal amount) {
        String data = merchantCode + "|" + orderId + "|" + amount + "|" + secretKey;
        return hmacSHA256(data, secretKey);
    }
    
    private boolean verifyChecksum(CallbackData callbackData) {
        String data = callbackData.getTransId() + "|" + 
                      callbackData.getOrderId() + "|" + 
                      callbackData.getStatus() + "|" + 
                      callbackData.getMessage() + "|" + 
                      secretKey;
        String expectedChecksum = hmacSHA256(data, secretKey);
        return expectedChecksum.equals(callbackData.getChecksum());
    }
    
    private String hmacSHA256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating HMAC", e);
        }
    }
    
    private String encodeValue(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}