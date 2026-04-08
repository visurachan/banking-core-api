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

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<TransactionResponseDto> depositMoney(
            @Valid @RequestBody DepositRequestDto depositRequest){
        return  ResponseEntity.status(HttpStatus.CREATED).body(accountService.depositMoney(depositRequest));


    }

    @PostMapping("/{myAccountNumber}/transfer")
    public ResponseEntity<TransferResponseDto> transferMoney(
            @Valid @RequestBody TransferRequestDto transferRequest,
            @AuthenticationPrincipal String email,
            @PathVariable String myAccountNumber){

        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.transferMoney(email, myAccountNumber,transferRequest));

    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<TransactionResponseDto> withdrawMoney(
            @Valid @RequestBody WithdrawRequestDto withdrawRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.withdrawMoney(withdrawRequest));

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



