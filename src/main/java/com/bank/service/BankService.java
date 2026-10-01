package com.bank.service;

import com.bank.api.BankDtos.*;
import com.bank.model.*;
import com.bank.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class BankService {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    public BankService(AccountRepository accounts, TransactionRepository transactions) { this.accounts = accounts; this.transactions = transactions; }

    @Transactional(readOnly = true)
    public List<AccountResponse> accounts() { return accounts.findAll().stream().map(this::accountResponse).toList(); }
    @Transactional(readOnly = true)
    public AccountResponse account(String number) { return accountResponse(findAccount(number)); }
    @Transactional(readOnly = true)
    public List<TransactionResponse> history(String number) { return transactions.findByAccountIdOrderByCreatedAtDesc(findAccount(number).getId()).stream().map(this::transactionResponse).toList(); }
    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        BigDecimal total = accounts.findAll().stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new DashboardResponse(accounts.count(), money(total), transactions.findTop10ByOrderByCreatedAtDesc().stream().map(this::transactionResponse).toList());
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        if (accounts.existsByEmail(request.email().trim().toLowerCase())) throw new IllegalArgumentException("An account already uses this email address.");
        Account account = new Account();
        account.setAccountNumber("BA" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        account.setHolderName(request.holderName().trim()); account.setEmail(request.email().trim().toLowerCase());
        BigDecimal opening = money(request.openingBalance() == null ? BigDecimal.ZERO : request.openingBalance()); account.setBalance(opening);
        accounts.save(account);
        if (opening.signum() > 0) record(account, TransactionType.DEPOSIT, opening, null, "Opening balance");
        return accountResponse(account);
    }

    @Transactional
    public AccountResponse deposit(String number, AmountRequest request) {
        Account account = findAccount(number); BigDecimal amount = money(request.amount());
        account.setBalance(account.getBalance().add(amount)); record(account, TransactionType.DEPOSIT, amount, null, request.reference());
        return accountResponse(account);
    }

    @Transactional
    public AccountResponse withdraw(String number, AmountRequest request) {
        Account account = findAccount(number); BigDecimal amount = money(request.amount());
        if (account.getBalance().compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient funds for this withdrawal.");
        account.setBalance(account.getBalance().subtract(amount)); record(account, TransactionType.WITHDRAWAL, amount, null, request.reference());
        return accountResponse(account);
    }

    @Transactional
    public void transfer(TransferRequest request) {
        if (request.fromAccountNumber().equalsIgnoreCase(request.toAccountNumber())) throw new IllegalArgumentException("Choose two different accounts for a transfer.");
        Account source = findAccount(request.fromAccountNumber()); Account destination = findAccount(request.toAccountNumber()); BigDecimal amount = money(request.amount());
        if (source.getBalance().compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient funds for this transfer.");
        source.setBalance(source.getBalance().subtract(amount)); destination.setBalance(destination.getBalance().add(amount));
        record(source, TransactionType.TRANSFER_OUT, amount, destination.getAccountNumber(), request.reference());
        record(destination, TransactionType.TRANSFER_IN, amount, source.getAccountNumber(), request.reference());
    }

    private Account findAccount(String number) { return accounts.findByAccountNumber(number.trim()).orElseThrow(() -> new IllegalArgumentException("Account not found.")); }
    private void record(Account account, TransactionType type, BigDecimal amount, String counterparty, String reference) { Transaction transaction = new Transaction(); transaction.setAccount(account); transaction.setType(type); transaction.setAmount(amount); transaction.setCounterpartyAccountNumber(counterparty); transaction.setReference(reference == null || reference.isBlank() ? null : reference.trim()); transactions.save(transaction); }
    private BigDecimal money(BigDecimal amount) { return amount.setScale(2, RoundingMode.HALF_UP); }
    private AccountResponse accountResponse(Account a) { return new AccountResponse(a.getId(), a.getAccountNumber(), a.getHolderName(), a.getEmail(), money(a.getBalance()), a.getCreatedAt()); }
    private TransactionResponse transactionResponse(Transaction t) { return new TransactionResponse(t.getId(), t.getAccount().getAccountNumber(), t.getType(), money(t.getAmount()), t.getCounterpartyAccountNumber(), t.getReference(), t.getCreatedAt()); }
}
