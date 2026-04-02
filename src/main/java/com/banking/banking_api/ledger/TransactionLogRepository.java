package com.banking.banking_api.ledger;

import com.banking.banking_api.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {
    List<TransactionLog> findByFromAccountOrToAccount(Account fromAccount, Account toAccount);

}
