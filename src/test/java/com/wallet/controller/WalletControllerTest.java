package com.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.model.Transaction;
import com.wallet.model.TransactionType;
import com.wallet.model.Wallet;
import com.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    private static final String USER_ID = "user123";
    private static final String BASE_URL = "/api/wallets";

    @Test
    void createWallet_Success() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setUserId(USER_ID);
        wallet.setBalance(BigDecimal.ZERO);

        when(walletService.createWallet(anyString())).thenReturn(wallet);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateWalletRequest(USER_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    void getBalance_Success() throws Exception {
        when(walletService.getBalance(USER_ID)).thenReturn(new BigDecimal("100.00"));

        mockMvc.perform(get(BASE_URL + "/{userId}/balance", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void getHistoricalBalance_Success() throws Exception {
        LocalDateTime timestamp = LocalDateTime.now();
        when(walletService.getHistoricalBalance(anyString(), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("100.00"));

        mockMvc.perform(get(BASE_URL + "/{userId}/history", USER_ID)
                .param("timestamp", timestamp.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void depositFunds_Success() throws Exception {
        when(walletService.depositFunds(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(new BigDecimal("100.00"));

        mockMvc.perform(post(BASE_URL + "/{userId}/deposit", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransactionRequest(
                    new BigDecimal("100.00"), "Test deposit"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void withdrawFunds_Success() throws Exception {
        when(walletService.withdrawFunds(anyString(), any(BigDecimal.class), anyString()))
                .thenReturn(new BigDecimal("50.00"));

        mockMvc.perform(post(BASE_URL + "/{userId}/withdraw", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransactionRequest(
                    new BigDecimal("50.00"), "Test withdrawal"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.00));
    }

    @Test
    void transferFunds_Success() throws Exception {
        mockMvc.perform(post(BASE_URL + "/{fromUserId}/transfer/{toUserId}", "fromUser", "toUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransactionRequest(
                    new BigDecimal("25.00"), "Test transfer"))))
                .andExpect(status().isOk());
    }

    @Test
    void getTransactionHistory_Success() throws Exception {
        List<Transaction> transactions = Arrays.asList(
            createTransaction(TransactionType.DEPOSIT, new BigDecimal("100.00")),
            createTransaction(TransactionType.WITHDRAWAL, new BigDecimal("50.00"))
        );

        when(walletService.getTransactionHistory(USER_ID)).thenReturn(transactions);

        mockMvc.perform(get(BASE_URL + "/{userId}/transactions", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[1].type").value("WITHDRAWAL"));
    }

    private Transaction createTransaction(TransactionType type, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setAmount(amount);
        return transaction;
    }
} 