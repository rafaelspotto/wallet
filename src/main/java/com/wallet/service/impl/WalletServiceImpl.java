package com.wallet.service.impl;

import com.wallet.exception.WalletException;
import com.wallet.model.Transaction;
import com.wallet.model.TransactionType;
import com.wallet.model.Wallet;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import com.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Wallet createWallet(String userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new WalletException("Wallet already exists for user: " + userId);
        }
        return walletRepository.save(new Wallet());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(String userId) {
        Wallet wallet = getWalletByUserId(userId);
        return wallet.getBalance();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getHistoricalBalance(String userId, LocalDateTime timestamp) {
        Wallet wallet = getWalletByUserId(userId);
        return transactionRepository
            .findFirstByWalletIdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(wallet.getId(), timestamp)
            .map(Transaction::getBalance)
            .orElse(wallet.getBalance());
    }

    @Override
    @Transactional
    public BigDecimal depositFunds(String userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletException("Deposit amount must be greater than zero");
        }

        Wallet wallet = getWalletByUserId(userId);
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        createTransaction(wallet, TransactionType.DEPOSIT, amount, newBalance, description);
        return newBalance;
    }

    @Override
    @Transactional
    public BigDecimal withdrawFunds(String userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletException("Withdrawal amount must be greater than zero");
        }

        Wallet wallet = getWalletByUserId(userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new WalletException("Insufficient funds");
        }

        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        createTransaction(wallet, TransactionType.WITHDRAWAL, amount, newBalance, description);
        return newBalance;
    }

    @Override
    @Transactional
    public void transferFunds(String fromUserId, String toUserId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletException("Transfer amount must be greater than zero");
        }

        Wallet fromWallet = getWalletByUserId(fromUserId);
        Wallet toWallet = getWalletByUserId(toUserId);

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new WalletException("Insufficient funds for transfer");
        }

        BigDecimal fromNewBalance = fromWallet.getBalance().subtract(amount);
        BigDecimal toNewBalance = toWallet.getBalance().add(amount);

        fromWallet.setBalance(fromNewBalance);
        toWallet.setBalance(toNewBalance);

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        createTransaction(fromWallet, TransactionType.TRANSFER, amount.negate(), fromNewBalance,
            "Transfer to " + toUserId + ": " + description);
        createTransaction(toWallet, TransactionType.TRANSFER, amount, toNewBalance,
            "Transfer from " + fromUserId + ": " + description);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionHistory(String userId) {
        Wallet wallet = getWalletByUserId(userId);
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }

    private Wallet getWalletByUserId(String userId) {
        return walletRepository.findByUserId(userId)
            .orElseThrow(() -> new WalletException("Wallet not found for user: " + userId));
    }

    private void createTransaction(Wallet wallet, TransactionType type, BigDecimal amount,
                                 BigDecimal balance, String description) {
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalance(balance);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }
} 