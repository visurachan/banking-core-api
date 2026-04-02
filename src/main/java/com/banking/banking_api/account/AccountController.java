package com.banking.banking_api.account;

import com.banking.banking_api.account.Dto.AccountResponseDto;
import io.jsonwebtoken.Jwt;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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


}



