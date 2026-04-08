package com.banking.banking_api.ledger;

import com.banking.banking_api.account.Account;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM LedgerEntry e "+
            "WHERE e.account = :account AND e.entryType = :entryType")
    BigDecimal sumByAccountAndEntryType(@Param("account") Account account,
                                        @Param("entryType") EntryType entryType);




}
