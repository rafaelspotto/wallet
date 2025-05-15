package com.wallet.service;

import com.wallet.model.Wallet;
import com.wallet.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface WalletService {
    Wallet createWallet(String userId);
    BigDecimal getBalance(String userId);
    BigDecimal getHistoricalBalance(String userId, LocalDateTime timestamp);
    BigDecimal depositFunds(String userId, BigDecimal amount, String description);
    BigDecimal withdrawFunds(String userId, BigDecimal amount, String description);
    void transferFunds(String fromUserId, String toUserId, BigDecimal amount, String description);
    List<Transaction> getTransactionHistory(String userId);
} 