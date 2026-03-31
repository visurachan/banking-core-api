package com.banking.banking_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for user login")
public record LoginRequestDto(

    @Schema(
            description = "User's email address",
            example = "john@test.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message =  "Email is required")
    @Email(message ="Email must be valid")
    String email,

    @Schema(
            description = "User's password",
            example = "YourPass123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password is required")
    String password

){}