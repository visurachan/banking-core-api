package com.banking.banking_api.account.Dto;

import com.banking.banking_api.account.AccountStatus;
import com.banking.banking_api.account.AccountType;
import com.banking.banking_api.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response payload for account creation")
public record AccountResponseDto(


        @Schema(description = "Account Number", example = "12345678")
        String accountNumber,

        @Schema(description = "Account Type", example = "CURRENT")
        AccountType accountType,

        @Schema(description = "Currency", example = "GBP")
        String currency,

        @Schema(description = "Account Status", example = "ACTIVE")
        AccountStatus accountStatus,

        @Schema(description = "Account creation timestamp", example = "2026-03-29T10:30:00")
        LocalDateTime createdAt

) {}
