package ru.rest.wallet.dto;

import jakarta.validation.constraints.NotNull;
import ru.rest.wallet.entity.Currency;

public record CreateWalletRequest(
        @NotNull
        Long userId,

        @NotNull
        Currency currency
) {
}
