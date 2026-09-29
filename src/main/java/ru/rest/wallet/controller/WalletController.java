package ru.rest.wallet.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.rest.wallet.dto.BalanceOperationRequest;
import ru.rest.wallet.dto.CreateWalletRequest;
import ru.rest.wallet.dto.TransactionResponse;
import ru.rest.wallet.dto.WalletResponse;
import ru.rest.wallet.service.WalletService;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse createWallet(
            @Valid @RequestBody CreateWalletRequest request
    ) {
        return walletService.createWallet(request);
    }

    @GetMapping("/{walletId}")
    public WalletResponse getWallet(
            @PathVariable Long walletId
    ) {
        return walletService.getWallet(walletId);
    }

    @GetMapping("/user/{userId}")
    public WalletResponse getWalletByUser(
            @PathVariable Long userId
    ) {
        return walletService.getWalletByUser(userId);
    }

    @PostMapping("/{walletId}/deposit")
    public WalletResponse deposit(
            @PathVariable Long walletId,
            @Valid @RequestBody BalanceOperationRequest request
    ) {

        return walletService.deposit(
                walletId,
                request
        );
    }
    @PostMapping("/{walletId}/withdraw")
    public WalletResponse withdraw(
            @PathVariable Long walletId,
            @Valid @RequestBody BalanceOperationRequest request
    ) {

        return walletService.withdraw(
                walletId,
                request
        );
    }
    @GetMapping("/{walletId}/transactions")
    public List<TransactionResponse> getTransactions(
            @PathVariable Long walletId
    ) {

        return walletService.getTransactions(walletId);
    }
}