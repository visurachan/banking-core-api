package com.banking.banking_api.account;

import com.banking.banking_api.account.Dto.*;
import io.jsonwebtoken.Jwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("api/v1/account")
@AllArgsConstructor
@Tag(name = "Account", description = "Account management and transaction endpoints")

public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create a new current account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/newCurrentAccount")
    public ResponseEntity<AccountResponseDto> createNewCurrentAccount(@AuthenticationPrincipal String email) {
        AccountResponseDto accountResponse = accountService.createNewCurrentAccount(email);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }

    @Operation(summary = "Get all accounts for logged-in user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/myAllAccounts")
    public ResponseEntity<List<AccountResponseDto>> getMyALlAccounts(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(accountService.getAccountsByUser(email));
    }

    @Operation(summary = "Get details for a specific account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "You do not own this account"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/accountDetails")
    public ResponseEntity<AccountResponseDto> getMyAccountDetails(
            @AuthenticationPrincipal String email,
            @RequestParam String accountNumber) {
        return ResponseEntity.ok(accountService.getMyAccountDetails(email, accountNumber));
    }

    @Operation(summary = "Deposit funds into an account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Deposit successful"),
            @ApiResponse(responseCode = "400", description = "Account is not active"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDto> depositMoney(
            @Parameter(
                    description = "Unique key to prevent duplicate transactions. Generate one by running 'uuidgen' in terminal or use an online UUID generator (e.g. uuidgenerator.net)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DepositRequestDto depositRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.depositMoney(idempotencyKey, depositRequest));
    }

    @Operation(summary = "Withdraw funds from an account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Withdrawal successful"),
            @ApiResponse(responseCode = "400", description = "Account is not active"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Insufficient funds")
    })
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDto> withdrawMoney(
            @Parameter(
                    description = "Unique key to prevent duplicate transactions. Generate one by running 'uuidgen' in terminal or use an online UUID generator (e.g. uuidgenerator.net)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody WithdrawRequestDto withdrawRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.withdrawMoney(idempotencyKey, withdrawRequest));
    }

    @Operation(summary = "Transfer funds to another account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transfer successful"),
            @ApiResponse(responseCode = "400", description = "Account is not active or same account transfer"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "You do not own this account"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "409", description = "Concurrent transaction conflict, please try again"),
            @ApiResponse(responseCode = "422", description = "Insufficient funds")
    })
    @PostMapping("/{myAccountNumber}/transfer")
    public ResponseEntity<TransferResponseDto> transferMoney(
            @Parameter(
                    description = "Unique key to prevent duplicate transactions. Generate one by running 'uuidgen' in terminal or use an online UUID generator (e.g. uuidgenerator.net)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequestDto transferRequest,
            @AuthenticationPrincipal String email,
            @PathVariable String myAccountNumber) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.transferMoney(idempotencyKey, email, myAccountNumber, transferRequest));
    }

    @Operation(summary = "Get paginated transaction history for an account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "You do not own this account"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @Parameters({
            @Parameter(name = "page", description = "Page number", example = "0"),
            @Parameter(name = "size", description = "Page size", example = "10"),
            @Parameter(name = "sort", description = "Sort field", example = "createdAt,desc")
    })
    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<Page<TransactionHistoryDto>> getTransactionHistory(
            @AuthenticationPrincipal String email,
            @PathVariable String accountNumber,
            @Parameter(hidden = true) @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(accountService.getTransactionHistory(email, accountNumber, pageable));
    }
}
