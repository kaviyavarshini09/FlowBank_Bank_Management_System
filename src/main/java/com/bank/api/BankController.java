package com.bank.api;

import com.bank.api.BankDtos.*;
import com.bank.service.BankService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BankController {
    private final BankService bank;
    public BankController(BankService bank) { this.bank = bank; }
    @GetMapping("/dashboard") public DashboardResponse dashboard() { return bank.dashboard(); }
    @GetMapping("/accounts") public List<AccountResponse> accounts() { return bank.accounts(); }
    @PostMapping("/accounts") @ResponseStatus(HttpStatus.CREATED) public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) { return bank.create(request); }
    @GetMapping("/accounts/{number}") public AccountResponse account(@PathVariable String number) { return bank.account(number); }
    @GetMapping("/accounts/{number}/transactions") public List<TransactionResponse> history(@PathVariable String number) { return bank.history(number); }
    @PostMapping("/accounts/{number}/deposit") public AccountResponse deposit(@PathVariable String number, @Valid @RequestBody AmountRequest request) { return bank.deposit(number, request); }
    @PostMapping("/accounts/{number}/withdraw") public AccountResponse withdraw(@PathVariable String number, @Valid @RequestBody AmountRequest request) { return bank.withdraw(number, request); }
    @PostMapping("/transfers") @ResponseStatus(HttpStatus.NO_CONTENT) public void transfer(@Valid @RequestBody TransferRequest request) { bank.transfer(request); }
}
