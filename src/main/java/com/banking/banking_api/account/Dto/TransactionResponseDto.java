package com.banking.banking_api.account.Dto;

import com.banking.banking_api.ledger.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponseDto(
        String accountNumber,
        String reference,
        TransactionType transactionType,
        BigDecimal amount,
        String currency,
        BigDecimal newBalance,
        LocalDateTime timeStamp

) { }
