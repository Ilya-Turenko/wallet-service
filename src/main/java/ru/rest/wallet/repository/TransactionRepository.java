package ru.rest.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rest.wallet.entity.WalletTransaction;

import java.util.List;

public interface TransactionRepository
        extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction>
    findAllByWalletIdOrderByCreatedAtDesc(Long walletId);
}