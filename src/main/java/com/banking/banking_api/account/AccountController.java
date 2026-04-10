package com.banking.banking_api.account;

import com.banking.banking_api.account.Dto.*;
import io.jsonwebtoken.Jwt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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

public class AccountController {

    private final AccountService accountService;

    @PostMapping("/newCurrentAccount")
    public ResponseEntity<AccountResponseDto> createNewCurrentAccount(@AuthenticationPrincipal String email){

        AccountResponseDto accountResponse = accountService.createNewCurrentAccount(email);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }

    @GetMapping("/myAllAccounts")
    public ResponseEntity<List<AccountResponseDto>>getMyALlAccounts(@AuthenticationPrincipal String email){

        return ResponseEntity.ok(accountService.getAccountsByUser(email));
    }

    @GetMapping("/accountDetails")
    public ResponseEntity<AccountResponseDto> getMyAccountDetails(
            @AuthenticationPrincipal String email,
            @RequestParam String accountNumber){
        return ResponseEntity.ok(accountService.getMyAccountDetails(email, accountNumber));
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDto> depositMoney(
            @Parameter(
                    description = "Unique key to prevent duplicate transactions. Generate one by running 'uuidgen' in terminal or use an online UUID generator (e.g. uuidgenerator.net)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DepositRequestDto depositRequest){
        return  ResponseEntity.status(HttpStatus.CREATED).body(accountService.depositMoney(idempotencyKey,depositRequest));


    }

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
            @PathVariable String myAccountNumber){

        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.transferMoney(idempotencyKey,email, myAccountNumber,transferRequest));

    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDto> withdrawMoney(
            @Parameter(
                    description = "Unique key to prevent duplicate transactions. Generate one by running 'uuidgen' in terminal or use an online UUID generator (e.g. uuidgenerator.net)",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody WithdrawRequestDto withdrawRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.withdrawMoney(idempotencyKey,withdrawRequest));

    }

    @Operation(summary = "Get transaction history")
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



