package ru.rest.wallet.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rest.wallet.dto.BalanceOperationRequest;
import ru.rest.wallet.dto.CreateWalletRequest;
import ru.rest.wallet.dto.TransactionResponse;
import ru.rest.wallet.dto.WalletResponse;
import ru.rest.wallet.entity.TransactionType;
import ru.rest.wallet.entity.User;
import ru.rest.wallet.entity.Wallet;
import ru.rest.wallet.entity.WalletTransaction;
import ru.rest.wallet.exception.InsufficientFundsException;
import ru.rest.wallet.exception.UserNotFoundException;
import ru.rest.wallet.exception.WalletAlreadyExistsException;
import ru.rest.wallet.exception.WalletNotFoundException;
import ru.rest.wallet.repository.TransactionRepository;
import ru.rest.wallet.repository.UserRepository;
import ru.rest.wallet.repository.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;



    public WalletService(
            WalletRepository walletRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            CurrentUserService currentUserService
    ) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public WalletResponse createWallet(
            CreateWalletRequest request
    ) {
        currentUserService.requireOwner(request.userId());

        User user =
                userRepository.findById(request.userId())
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        request.userId()
                                )
                        );

        if (walletRepository.existsByUserId(user.getId())) {

            throw new WalletAlreadyExistsException(
                    user.getId()
            );
        }

        Wallet wallet = new Wallet();

        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency(request.currency());

        Wallet savedWallet =
                walletRepository.save(wallet);

        return toResponse(savedWallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long walletId) {

        Wallet wallet =
                walletRepository.findById(walletId)
                        .orElseThrow(() ->
                                new WalletNotFoundException(
                                        walletId
                                )
                        );
        currentUserService.requireOwner(wallet.getUser().getId());

        return toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletByUser(Long userId) {
        currentUserService.requireOwner(userId);

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "У пользователя нет кошелька"
                        )
                );

        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse deposit(
            Long walletId,
            BalanceOperationRequest request
    ) {

        Wallet wallet =
                walletRepository.findByIdForUpdate(walletId)
                        .orElseThrow(() ->
                                new WalletNotFoundException(
                                        walletId
                                )
                        );
        currentUserService.requireOwner(wallet.getUser().getId());

        BigDecimal newBalance =
                wallet.getBalance()
                        .add(request.amount());

        wallet.setBalance(newBalance);

        walletRepository.save(wallet);

        WalletTransaction transaction =
                new WalletTransaction();

        transaction.setWallet(wallet);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(request.amount());
        transaction.setBalanceAfter(newBalance);

        transactionRepository.save(transaction);

        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse withdraw(
            Long walletId,
            BalanceOperationRequest request
    ) {

        Wallet wallet =
                walletRepository.findByIdForUpdate(walletId)
                        .orElseThrow(() ->
                                new WalletNotFoundException(
                                        walletId
                                )
                        );
        currentUserService.requireOwner(wallet.getUser().getId());
        BigDecimal amount = request.amount();

        if (wallet.getBalance()
                .compareTo(amount) < 0) {

            throw new InsufficientFundsException(
                    wallet.getBalance(),
                    amount
            );
        }

        BigDecimal newBalance =
                wallet.getBalance().subtract(amount);

        wallet.setBalance(newBalance);

        walletRepository.save(wallet);

        WalletTransaction transaction =
                new WalletTransaction();

        transaction.setWallet(wallet);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(newBalance);

        transactionRepository.save(transaction);

        return toResponse(wallet);
    }

    private TransactionResponse toTransactionResponse(
            WalletTransaction transaction
    ) {

        Long relatedWalletId = null;

        if (transaction.getRelatedWallet() != null) {
            relatedWalletId =
                    transaction.getRelatedWallet().getId();
        }

        return new TransactionResponse(
                transaction.getId(),
                transaction.getWallet().getId(),
                relatedWalletId,
                transaction.getTransferId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getCreatedAt()
        );
    }
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(
            Long walletId
    ) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new WalletNotFoundException(walletId)
                );

        currentUserService.requireOwner(wallet.getUser().getId());

        return transactionRepository
                .findAllByWalletIdOrderByCreatedAtDesc(walletId)
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    private WalletResponse toResponse(Wallet wallet) {

        return new WalletResponse(
                wallet.getId(),
                wallet.getUser().getId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getCreatedAt()
        );
    }
}