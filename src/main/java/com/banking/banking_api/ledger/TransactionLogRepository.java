package com.banking.banking_api.ledger;

import com.banking.banking_api.account.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {


    @Query("SELECT t FROM TransactionLog t JOIN FETCH t.fromAccount JOIN FETCH t.toAccount " +
            "WHERE (t.fromAccount = :account) " +
            "OR (t.toAccount = :account AND t.status = 'COMPLETED')")
    Page<TransactionLog> findByFromAccountOrToAccount(@Param("account") Account fromAccount, Pageable pageable);
}
