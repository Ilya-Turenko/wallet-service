package ru.rest.wallet.exception;

public class WalletAlreadyExistsException
        extends RuntimeException {

    public WalletAlreadyExistsException(Long userId) {
        super(
                "User already has a wallet: "
                        + userId
        );
    }
}