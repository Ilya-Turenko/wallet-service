package ru.rest.wallet.exception;

import ru.rest.wallet.entity.Currency;

public class CurrencyMismatchException
        extends RuntimeException {

    public CurrencyMismatchException(
            Currency from,
            Currency to
    ) {

        super(
                "Wallet currencies do not match: "
                        + from
                        + " -> "
                        + to
        );
    }
}