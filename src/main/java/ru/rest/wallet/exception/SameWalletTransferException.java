package ru.rest.wallet.exception;

public class SameWalletTransferException
        extends RuntimeException {

    public SameWalletTransferException() {
        super(
                "Cannot transfer money to the same wallet"
        );
    }
}