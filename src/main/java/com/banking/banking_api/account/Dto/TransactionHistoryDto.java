package com.banking.banking_api.account.Dto;

import com.banking.banking_api.ledger.EntryType;
import com.banking.banking_api.ledger.TransactionStatus;
import com.banking.banking_api.ledger.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionHistoryDto(
        String reference,
        TransactionType transactionType,
        TransactionStatus transactionStatus,
        BigDecimal amount,
        String currency,
        String fromAccount, // Null in deposits, withdraws
        String toAccount, // Null in deposits, withdraws
        String direction, // Null in deposits and withdraws, IN/OUT for transfers
        String description,
        LocalDateTime createdAt

)
{ }
