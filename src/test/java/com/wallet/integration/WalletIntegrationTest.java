package com.wallet.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.model.Transaction;
import com.wallet.model.TransactionType;
import com.wallet.model.Wallet;
import com.wallet.repository.TransactionRepository;
import com.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WalletIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final String USER_ID = "testUser";
    private static final String BASE_URL = "/api/wallets";

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @Test
    void completeWalletFlow() throws Exception {
        // 1. Create wallet
        String response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateWalletRequest(USER_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.balance").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Wallet wallet = objectMapper.readValue(response, Wallet.class);
        assertNotNull(wallet);

        // 2. Deposit funds
        mockMvc.perform(post(BASE_URL + "/{userId}/deposit", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransactionRequest(
                    new BigDecimal("100.00"), "Initial deposit"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));

        // 3. Check balance
        mockMvc.perform(get(BASE_URL + "/{userId}/balance", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));

        // 4. Withdraw funds
        mockMvc.perform(post(BASE_URL + "/{userId}/withdraw", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransactionRequest(
                    new BigDecimal("50.00"), "Withdrawal"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.00));

        // 5. Create second wallet for transfer
        String secondUserId = "secondUser";
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateWalletRequest(secondUserId))))
                .andExpect(status().isOk());

        // 6. Transfer funds
        mockMvc.perform(post(BASE_URL + "/{fromUserId}/transfer/{toUserId}", USER_ID, secondUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransactionRequest(
                    new BigDecimal("25.00"), "Transfer"))))
                .andExpect(status().isOk());

        // 7. Verify final balances
        mockMvc.perform(get(BASE_URL + "/{userId}/balance", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(25.00));

        mockMvc.perform(get(BASE_URL + "/{userId}/balance", secondUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(25.00));

        // 8. Check transaction history
        List<Transaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
        assertEquals(3, transactions.size());
        assertEquals(TransactionType.DEPOSIT, transactions.get(0).getType());
        assertEquals(TransactionType.WITHDRAWAL, transactions.get(1).getType());
        assertEquals(TransactionType.TRANSFER, transactions.get(2).getType());
    }

    private record CreateWalletRequest(String userId) {}
    private record TransactionRequest(BigDecimal amount, String description) {}
} 