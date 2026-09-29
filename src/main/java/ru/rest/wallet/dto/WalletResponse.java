package ru.rest.wallet.dto;

import ru.rest.wallet.entity.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(
        Long id,
        Long userId,
        BigDecimal balance,
        Currency currency,
        LocalDateTime createdAt
) {
}
