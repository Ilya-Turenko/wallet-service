package ru.rest.wallet.dto;

import ru.rest.wallet.entity.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferResponse(
        UUID transferId,

        Long fromWalletId,
        Long toWalletId,

        BigDecimal amount,
        Currency currency,

        BigDecimal fromBalance,
        BigDecimal toBalance,

        LocalDateTime createdAt

) {
}
