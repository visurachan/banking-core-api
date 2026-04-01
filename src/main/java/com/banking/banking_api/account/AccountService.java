package com.banking.banking_api.account;

import com.banking.banking_api.account.Dto.AccountResponseDto;
import com.banking.banking_api.user.UserRepository;
import io.jsonwebtoken.Jwt;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor

public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountResponseDto createNewCurrentAccount(String email) {


    }
}
