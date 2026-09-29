package ru.rest.wallet.dto;

import ru.rest.wallet.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(

        Long id,
        Long walletId,

        Long relatedWalletId,
        UUID transferId,

        TransactionType type,

        BigDecimal amount,
        BigDecimal balanceAfter,

        LocalDateTime createdAt

) {
}