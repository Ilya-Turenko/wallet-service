package ru.rest.wallet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rest.wallet.dto.TransferRequest;
import ru.rest.wallet.dto.TransferResponse;
import ru.rest.wallet.entity.TransactionType;
import ru.rest.wallet.entity.Wallet;
import ru.rest.wallet.entity.WalletTransaction;
import ru.rest.wallet.exception.CurrencyMismatchException;
import ru.rest.wallet.exception.InsufficientFundsException;
import ru.rest.wallet.exception.SameWalletTransferException;
import ru.rest.wallet.exception.WalletNotFoundException;
import ru.rest.wallet.repository.TransactionRepository;
import ru.rest.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository
    ) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransferResponse transfer(
            TransferRequest request
    ) {

        Long fromWalletId = request.fromWalletId();
        Long toWalletId = request.toWalletId();
        BigDecimal amount = request.amount();

        if (fromWalletId.equals(toWalletId)) {

            throw new SameWalletTransferException();
        }

        long firstId =
                Math.min(fromWalletId, toWalletId);

        long secondId =
                Math.max(fromWalletId, toWalletId);

        Wallet firstWallet =
                walletRepository.findByIdForUpdate(firstId)
                        .orElseThrow(() ->
                                new WalletNotFoundException(
                                        firstId
                                )
                        );

        Wallet secondWallet =
                walletRepository
                        .findByIdForUpdate(secondId)
                        .orElseThrow(() ->
                                new WalletNotFoundException(
                                        secondId
                                )
                        );

        Wallet fromWallet;
        Wallet toWallet;

        if (firstWallet.getId().equals(fromWalletId)) {
            fromWallet = firstWallet;
            toWallet = secondWallet;
        } else {
            fromWallet = secondWallet;
            toWallet = firstWallet;
        }

        if (fromWallet.getCurrency()
                != toWallet.getCurrency()) {

            throw new CurrencyMismatchException(
                    fromWallet.getCurrency(),
                    toWallet.getCurrency()
            );
        }

        if (fromWallet.getBalance()
                .compareTo(amount) < 0) {

            throw new InsufficientFundsException(
                    fromWallet.getBalance(),
                    amount
            );
        }


        BigDecimal newFromBalance =
                fromWallet.getBalance()
                        .subtract(amount);

        BigDecimal newToBalance =
                toWallet.getBalance()
                        .add(amount);

        fromWallet.setBalance(newFromBalance);
        toWallet.setBalance(newToBalance);

        UUID transferId = UUID.randomUUID();

        WalletTransaction outgoing =
                new WalletTransaction();

        outgoing.setWallet(fromWallet);
        outgoing.setRelatedWallet(toWallet);
        outgoing.setTransferId(transferId);
        outgoing.setType(
                TransactionType.TRANSFER_OUT
        );
        outgoing.setAmount(amount);
        outgoing.setBalanceAfter(
                newFromBalance
        );

        WalletTransaction incoming =
                new WalletTransaction();

        incoming.setWallet(toWallet);
        incoming.setRelatedWallet(fromWallet);
        incoming.setTransferId(transferId);
        incoming.setType(
                TransactionType.TRANSFER_IN
        );
        incoming.setAmount(amount);
        incoming.setBalanceAfter(
                newToBalance
        );

        transactionRepository.save(outgoing);
        transactionRepository.save(incoming);

        return new TransferResponse(
                transferId,
                fromWallet.getId(),
                toWallet.getId(),
                amount,
                fromWallet.getCurrency(),
                newFromBalance,
                newToBalance,
                LocalDateTime.now()
        );
    }
}