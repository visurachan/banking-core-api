package com.banking.banking_api.auth.dto;

import com.banking.banking_api.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response payload for user registration")
public record RegisterResponseDto(

        @Schema(description = "User's ID", example = "1")
        Long id,

        @Schema(description = "User's email address", example = "john@test.com")
        String email,

        @Schema(description = "User's first name", example = "John")
        String firstName,

        @Schema(description = "User's last name", example = "Doe")
        String lastName,

        @Schema(description = "User's assigned role", example = "CUSTOMER")
        Role role,

        @Schema(description = "Account creation timestamp", example = "2026-03-29T10:30:00")
        LocalDateTime createdAt

) {}