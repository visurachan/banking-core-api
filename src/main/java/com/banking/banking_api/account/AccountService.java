package com.banking.banking_api.account;

import com.banking.banking_api.account.Dto.AccountResponseDto;
import com.banking.banking_api.common.exception.ResourceNotFoundException;
import com.banking.banking_api.ledger.EntryType;
import com.banking.banking_api.ledger.LedgerEntryRepository;
import com.banking.banking_api.user.User;
import com.banking.banking_api.user.UserRepository;
import io.jsonwebtoken.Jwt;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.util.List;

@Service
@AllArgsConstructor

public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final LedgerEntryRepository ledgerEntryRepository;

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
                BigDecimal.ZERO,
                saved.getAccountStatus(),
                saved.getCreatedAt());

    }

    public List<AccountResponseDto> getAccountsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: "+ email
                ));
        return accountRepository.findByUser(user)
                .stream()
                .map(account -> new AccountResponseDto(
                        account.getAccountNumber(),
                        account.getAccountType(),
                        account.getCurrency(),
                        calculateAccountBalance(account),
                        account.getAccountStatus(),
                        account.getCreatedAt()
                ))
                .toList();


    }


    private String generateAccountNumber() {
        Long nextVal = jdbcTemplate.queryForObject(
                "SELECT nextval('account_number_seq')", Long.class
        );
        return String.format("%08d", nextVal);
    }

    private BigDecimal calculateAccountBalance(Account account){
        BigDecimal credits = ledgerEntryRepository.sumByAccountAndEntryType(account, EntryType.CREDIT);
        BigDecimal debits = ledgerEntryRepository.sumByAccountAndEntryType(account,EntryType.DEBIT);
        return credits.subtract(debits);
    }


    public AccountResponseDto getMyAccountDetails(String email, String accountNumber)  {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (!accountRepository.existsByAccountNumberAndUser(accountNumber, user)) {
            throw new AccessDeniedException("You do not own this account");
        }

        return new AccountResponseDto(
                account.getAccountNumber(),
                account.getAccountType(),
                account.getCurrency(),
                calculateAccountBalance(account),
                account.getAccountStatus(),
                account.getCreatedAt()
        );
    }
}
