package ru.rest.wallet.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rest.wallet.dto.TransferRequest;
import ru.rest.wallet.entity.Currency;
import ru.rest.wallet.entity.TransactionType;
import ru.rest.wallet.entity.User;
import ru.rest.wallet.entity.Wallet;
import ru.rest.wallet.entity.WalletTransaction;
import ru.rest.wallet.repository.TransactionRepository;
import ru.rest.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Test
    void transferUpdatesBothBalancesAndRecordsHistory() {
        TransferService service = new TransferService(
                walletRepository,
                transactionRepository,
                currentUserService
        );

        Wallet sender = wallet(1L, 3L, "1000.00");
        Wallet recipient = wallet(2L, 4L, "200.00");

        when(walletRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(sender));

        when(walletRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(recipient));

        var response = service.transfer(
                new TransferRequest(
                        1L,
                        2L,
                        new BigDecimal("300.00")
                )
        );

        assertEquals(
                new BigDecimal("700.00"),
                sender.getBalance()
        );
        assertEquals(
                new BigDecimal("500.00"),
                recipient.getBalance()
        );
        assertEquals(sender.getBalance(), response.fromBalance());
        assertEquals(recipient.getBalance(), response.toBalance());

        verify(currentUserService).requireOwner(3L);

        ArgumentCaptor<WalletTransaction> captor =
                ArgumentCaptor.forClass(WalletTransaction.class);

        verify(transactionRepository, times(2))
                .save(captor.capture());

        var records = captor.getAllValues();

        WalletTransaction outgoing = records.stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER_OUT)
                .findFirst()
                .orElseThrow();

        WalletTransaction incoming = records.stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER_IN)
                .findFirst()
                .orElseThrow();

        assertSame(sender, outgoing.getWallet());
        assertSame(recipient, outgoing.getRelatedWallet());
        assertSame(recipient, incoming.getWallet());
        assertSame(sender, incoming.getRelatedWallet());

        assertEquals(new BigDecimal("300.00"), outgoing.getAmount());
        assertEquals(new BigDecimal("300.00"), incoming.getAmount());
        assertEquals(sender.getBalance(), outgoing.getBalanceAfter());
        assertEquals(recipient.getBalance(), incoming.getBalanceAfter());

        assertNotNull(response.transferId());
        assertEquals(response.transferId(), outgoing.getTransferId());
        assertEquals(response.transferId(), incoming.getTransferId());
    }

    private Wallet wallet(Long id, long userId, String balance) {
        User user = new User();
        user.setId(userId);

        Wallet wallet = new Wallet();
        wallet.setId(id);
        wallet.setUser(user);
        wallet.setCurrency(Currency.RUB);
        wallet.setBalance(new BigDecimal(balance));

        return wallet;
    }
}