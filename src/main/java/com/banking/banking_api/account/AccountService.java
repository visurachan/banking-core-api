package com.banking.banking_api.account;

import com.banking.banking_api.account.Dto.AccountResponseDto;
import com.banking.banking_api.common.exception.ResourceNotFoundException;
import com.banking.banking_api.user.User;
import com.banking.banking_api.user.UserRepository;
import io.jsonwebtoken.Jwt;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor

public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public AccountResponseDto createNewCurrentAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Account account = Account.builder()
                .user(user)
                .accountNumber(generateAccountNumber())
                .build();


        Account saved = accountRepository.save(account);
        return new AccountResponseDto(
                saved.getAccountNumber(),
                saved.getAccountType(),
                saved.getCurrency(),
                saved.getAccountStatus(),
                saved.getCreatedAt());

    }

    private String generateAccountNumber() {
        Long nextVal = jdbcTemplate.queryForObject(
                "SELECT nextval('account_number_seq')", Long.class
        );
        return String.format("%08d", nextVal);
    }

}
