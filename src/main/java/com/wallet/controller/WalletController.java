package com.wallet.controller;

import com.wallet.model.Transaction;
import com.wallet.model.Wallet;
import com.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Wallet management APIs")
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    @Operation(summary = "Create a new wallet")
    public ResponseEntity<Wallet> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity.ok(walletService.createWallet(request.getUserId()));
    }

    @GetMapping("/{userId}/balance")
    @Operation(summary = "Get current wallet balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable @NotBlank String userId) {
        return ResponseEntity.ok(new BalanceResponse(walletService.getBalance(userId)));
    }

    @GetMapping("/{userId}/history")
    @Operation(summary = "Get historical wallet balance")
    public ResponseEntity<BalanceResponse> getHistoricalBalance(
            @PathVariable @NotBlank String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp) {
        return ResponseEntity.ok(new BalanceResponse(
            walletService.getHistoricalBalance(userId, timestamp != null ? timestamp : LocalDateTime.now())
        ));
    }

    @PostMapping("/{userId}/deposit")
    @Operation(summary = "Deposit funds into wallet")
    public ResponseEntity<BalanceResponse> depositFunds(
            @PathVariable @NotBlank String userId,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(new BalanceResponse(
            walletService.depositFunds(userId, request.getAmount(), request.getDescription())
        ));
    }

    @PostMapping("/{userId}/withdraw")
    @Operation(summary = "Withdraw funds from wallet")
    public ResponseEntity<BalanceResponse> withdrawFunds(
            @PathVariable @NotBlank String userId,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(new BalanceResponse(
            walletService.withdrawFunds(userId, request.getAmount(), request.getDescription())
        ));
    }

    @PostMapping("/{fromUserId}/transfer/{toUserId}")
    @Operation(summary = "Transfer funds between wallets")
    public ResponseEntity<Void> transferFunds(
            @PathVariable @NotBlank String fromUserId,
            @PathVariable @NotBlank String toUserId,
            @Valid @RequestBody TransactionRequest request) {
        walletService.transferFunds(fromUserId, toUserId, request.getAmount(), request.getDescription());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/transactions")
    @Operation(summary = "Get wallet transaction history")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable @NotBlank String userId) {
        return ResponseEntity.ok(walletService.getTransactionHistory(userId));
    }
}

record CreateWalletRequest(@NotBlank String userId) {}

record TransactionRequest(
    @NotNull @Positive BigDecimal amount,
    String description
) {}

record BalanceResponse(BigDecimal balance) {} 