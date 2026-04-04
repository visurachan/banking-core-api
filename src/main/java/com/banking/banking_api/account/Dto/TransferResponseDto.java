package com.banking.banking_api.account.Dto;

import com.banking.banking_api.ledger.TransactionStatus;
import com.banking.banking_api.ledger.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransferResponseDto(

        String fromAccountNumber,
        String toAccountNumber,
        String reference,
        TransactionType transactionType,
        TransactionStatus transactionStatus,
        BigDecimal amount,
        String currency,
        BigDecimal newBalance, // Null in Failed Scenario
        String failedReason,   // Null in Success Scenario
        LocalDateTime timeStamp

) { }


