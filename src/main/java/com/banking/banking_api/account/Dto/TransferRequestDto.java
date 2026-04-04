package com.banking.banking_api.account.Dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequestDto(
        @NotNull(message = "Amount is required")
        @DecimalMin(value= "0.01", message = "Amount is required")
        BigDecimal amount,

        String note,

        @NotBlank(message = "Account number is required")
        String toAccountNumber

) {
}
