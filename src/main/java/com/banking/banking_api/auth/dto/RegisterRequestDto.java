package com.banking.banking_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static io.lettuce.core.pubsub.PubSubOutput.Type.message;

@Schema(description =  "Request payload for user registration")
public record  RegisterRequestDto (

    @Schema(
            description = "User's email address",
            example = "john@test.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @Schema(
            description = "User's password (min 8 characters)",
            example = "SecurePass123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 8
    )
    @NotBlank(message = "Password is required")
    @Size(min = 8, message =  "Password must be atleast 8 characters")
    String password,

    @Schema(
            description = "User's First Name",
            example = "John",
            requiredMode = Schema.RequiredMode.REQUIRED

    )
    @NotBlank(message = "First Name is required")
    String firstName,

    @Schema(
            description = "User's Last Name",
            example = "Doe",
            requiredMode =  Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Last Name is required")
    String lastName,

    @Schema(
            description = "User's Phone Number",
            example = "0712345678",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String phone






){}


