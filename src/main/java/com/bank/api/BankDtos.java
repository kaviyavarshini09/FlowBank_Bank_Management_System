package com.bank.api;

import com.bank.model.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class BankDtos {
    private BankDtos() {}
    public record CreateAccountRequest(@NotBlank String holderName, @Email @NotBlank String email, @DecimalMin(value = "0.0") BigDecimal openingBalance) {}
    public record AmountRequest(@NotNull @DecimalMin(value = "0.01") BigDecimal amount, @Size(max = 140) String reference) {}
    public record TransferRequest(@NotBlank String fromAccountNumber, @NotBlank String toAccountNumber, @NotNull @DecimalMin(value = "0.01") BigDecimal amount, @Size(max = 140) String reference) {}
    public record AccountResponse(Long id, String accountNumber, String holderName, String email, BigDecimal balance, Instant createdAt) {}
    public record TransactionResponse(Long id, String accountNumber, TransactionType type, BigDecimal amount, String counterpartyAccountNumber, String reference, Instant createdAt) {}
    public record DashboardResponse(long accountCount, BigDecimal totalBalance, List<TransactionResponse> recentTransactions) {}
}
