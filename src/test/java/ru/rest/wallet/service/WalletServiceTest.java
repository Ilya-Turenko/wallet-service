package ru.rest.wallet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.rest.wallet.dto.BalanceOperationRequest;
import ru.rest.wallet.dto.WalletResponse;
import ru.rest.wallet.entity.Currency;
import ru.rest.wallet.entity.TransactionType;
import ru.rest.wallet.entity.User;
import ru.rest.wallet.entity.Wallet;
import ru.rest.wallet.exception.InsufficientFundsException;
import ru.rest.wallet.repository.TransactionRepository;
import ru.rest.wallet.repository.UserRepository;
import ru.rest.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrentUserService currentUserService;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(
                walletRepository,
                userRepository,
                transactionRepository,
                currentUserService
        );
    }

    private Wallet createWallet() {
        User user = new User();
        user.setId(3L);

        Wallet wallet = new Wallet();
        wallet.setId(4L);
        wallet.setUser(user);
        wallet.setCurrency(Currency.RUB);
        wallet.setBalance(new BigDecimal("1000.00"));

        return wallet;
    }

    @Test
    void withdrawUpdatesBalanceAndSavesTransaction() {
        Wallet wallet = createWallet();

        when(walletRepository.findByIdForUpdate(4L))
                .thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.withdraw(
                4L,
                new BalanceOperationRequest(
                        new BigDecimal("700.00")
                )
        );

        assertEquals(
                new BigDecimal("300.00"),
                response.balance()
        );

        verify(currentUserService).requireOwner(3L);

        verify(transactionRepository).save(argThat(transaction ->
                transaction.getWallet() == wallet
                        && transaction.getType() == TransactionType.WITHDRAW
                        && new BigDecimal("700.00")
                        .equals(transaction.getAmount())
                        && new BigDecimal("300.00")
                        .equals(transaction.getBalanceAfter())
        ));
    }

    @Test
    void withdrawWithInsufficientFundsDoesNotChangeBalance() {
        Wallet wallet = createWallet();

        when(walletRepository.findByIdForUpdate(4L))
                .thenReturn(Optional.of(wallet));

        assertThrows(
                InsufficientFundsException.class,
                () -> walletService.withdraw(
                        4L,
                        new BalanceOperationRequest(
                                new BigDecimal("1500.00")
                        )
                )
        );

        assertEquals(
                new BigDecimal("1000.00"),
                wallet.getBalance()
        );

        verify(walletRepository, never()).save(any(Wallet.class));
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void withdrawFromForeignWalletIsForbidden() {
        Wallet wallet = createWallet();

        when(walletRepository.findByIdForUpdate(4L))
                .thenReturn(Optional.of(wallet));

        doThrow(new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Нет доступа к чужому кошельку"
        )).when(currentUserService).requireOwner(3L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> walletService.withdraw(
                        4L,
                        new BalanceOperationRequest(
                                new BigDecimal("100.00")
                        )
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                wallet.getBalance()
        );

        verify(walletRepository, never()).save(any(Wallet.class));
        verifyNoInteractions(transactionRepository);
    }
}