package com.example.sepay.repository;

import com.example.sepay.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Transaction findByOrderId(String orderId);
}
