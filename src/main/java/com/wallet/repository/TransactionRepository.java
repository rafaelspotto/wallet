package com.wallet.repository;

import com.wallet.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    Optional<Transaction> findFirstByWalletIdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
        Long walletId, LocalDateTime timestamp);
} 