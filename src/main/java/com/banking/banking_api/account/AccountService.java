package com.banking.banking_api.account;

import com.banking.banking_api.account.Dto.*;
import com.banking.banking_api.common.exception.InsufficientFundsException;
import com.banking.banking_api.common.exception.ResourceNotFoundException;
import com.banking.banking_api.common.idempotency.IdempotencyService;
import com.banking.banking_api.common.util.TransactionReferenceGenerator;
import com.banking.banking_api.ledger.*;
import com.banking.banking_api.user.User;
import com.banking.banking_api.user.UserRepository;
import io.jsonwebtoken.Jwt;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor

public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionReferenceGenerator referenceGenerator;
    private final TransactionLogRepository transactionLogRepository;
    private final TransactionLogService transactionLogService;
    private final IdempotencyService idempotencyService;

    @Transactional
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

    @Transactional(readOnly = true)
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


    @Transactional(readOnly = true)
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


    @Transactional
    public TransactionResponseDto depositMoney(String idempotencyKey, DepositRequestDto depositRequest) {


        Optional<ResponseEntity<TransactionResponseDto>> stored =
                idempotencyService.getStoredResponse(idempotencyKey,TransactionResponseDto.class);
        if (stored.isPresent()){
            return stored.get().getBody();
        }

        Account account = accountRepository.findByAccountNumber(depositRequest.accountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if ( account.getAccountStatus()!= AccountStatus.ACTIVE){
            throw new IllegalStateException("Account is not active");
        }
        Account bankAccount = accountRepository.findByAccountNumber("BANK-INTERNAL")
                .orElseThrow(() -> new ResourceNotFoundException("bank internal account not found"));

        String transactionRef = referenceGenerator.generate();


        TransactionLog transactionLog = transactionLogService.savePending(
                TransactionLog.builder()
                        .reference(transactionRef)
                        .amount(depositRequest.amount())
                        .status(TransactionStatus.PENDING)
                        .description(depositRequest.note())
                        .fromAccount(bankAccount)
                        .toAccount(account)
                        .transactionType(TransactionType.DEPOSIT)
                        .build()



        );


        try {
            LedgerEntry bankDebit = LedgerEntry.builder()
                    .account(bankAccount)
                    .amount(depositRequest.amount())
                    .transactionLog(transactionLog)
                    .entryType(EntryType.DEBIT)
                    .description(depositRequest.note())
                    .build();

            LedgerEntry customerCredit = LedgerEntry.builder()
                    .account(account)
                    .amount(depositRequest.amount())
                    .transactionLog(transactionLog)
                    .entryType(EntryType.CREDIT)
                    .description(depositRequest.note())
                    .build();

            ledgerEntryRepository.save(bankDebit);
            ledgerEntryRepository.save(customerCredit);

            transactionLog.setStatus(TransactionStatus.COMPLETED);
            transactionLogRepository.save(transactionLog);


            BigDecimal newBalance = calculateAccountBalance(account);

            TransactionResponseDto response = new TransactionResponseDto(
                    account.getAccountNumber(),
                    transactionRef,
                    TransactionType.DEPOSIT,
                    depositRequest.amount(),
                    account.getCurrency(),
                    newBalance,
                    LocalDateTime.now()
            );

            idempotencyService.store(idempotencyKey, response, 201);

            return response;


        } catch (Exception e){
            transactionLogService.updateStatus(transactionLog,TransactionStatus.FAILED);
            throw e;

        }






    }
    @Transactional
    public TransferResponseDto transferMoney(String idempotencyKey,String email, String myAccountNumber,TransferRequestDto transferRequest) {

        Optional<ResponseEntity<TransferResponseDto>> stored =
                idempotencyService.getStoredResponse(idempotencyKey,TransferResponseDto.class);
        if (stored.isPresent()){
            return stored.get().getBody();
        }

        //Sender Account basic validation checks
        Account myAccount = accountRepository.findByAccountNumber(myAccountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("The Account you want to send money from is not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!accountRepository.existsByAccountNumberAndUser(myAccount.getAccountNumber(), user)) {
            throw new AccessDeniedException("You do not own the account you are trying to send money from");
        }
        if ( myAccount.getAccountStatus()!= AccountStatus.ACTIVE){
            throw new IllegalStateException("Your account is not active");
        }


        //Receiver account basic validation checks
        Account toAccount = accountRepository.findByAccountNumber(transferRequest.toAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("The Account you want to send money is not found"));
        if ( toAccount.getAccountStatus()!= AccountStatus.ACTIVE){
            throw new IllegalStateException("Account you are trying to send money is not active");
        }

        if (myAccount.getAccountNumber().equals(transferRequest.toAccountNumber())) {
            throw new IllegalStateException("Cannot transfer to the same account");
        }



        TransactionLog transactionLog = transactionLogService.savePending(
                TransactionLog.builder()
                        .reference(referenceGenerator.generate())
                        .amount(transferRequest.amount())
                        .status(TransactionStatus.PENDING)
                        .description(transferRequest.note())
                        .fromAccount(myAccount)
                        .toAccount(toAccount)
                        .transactionType(TransactionType.TRANSFER)
                        .build()



        );

        try {
            BigDecimal balance = calculateAccountBalance(myAccount);
            if(balance.compareTo(transferRequest.amount())<0){
                throw new InsufficientFundsException("Not enough funds");
            }



            LedgerEntry debitEntry = LedgerEntry.builder()
                    .account(myAccount)
                    .entryType(EntryType.DEBIT)
                    .description(transferRequest.note())
                    .amount(transferRequest.amount())
                    .transactionLog(transactionLog)
                    .build();

            LedgerEntry creditEntry = LedgerEntry.builder()
                    .account(toAccount)
                    .entryType(EntryType.CREDIT)
                    .description(transferRequest.note())
                    .amount(transferRequest.amount())
                    .transactionLog(transactionLog)
                    .build();

            ledgerEntryRepository.save(debitEntry);
            ledgerEntryRepository.save(creditEntry);

            transactionLog.setStatus(TransactionStatus.COMPLETED);
            transactionLogRepository.save(transactionLog);

            TransferResponseDto response = new TransferResponseDto(
                    myAccount.getAccountNumber(),
                    toAccount.getAccountNumber(),
                    transactionLog.getReference(),
                    TransactionType.TRANSFER,
                    TransactionStatus.COMPLETED,
                    transferRequest.amount(),
                    myAccount.getCurrency(),
                    calculateAccountBalance(myAccount),
                    null,
                    LocalDateTime.now()
            );


            idempotencyService.store(idempotencyKey, response, 201);
            return response;




        } catch (InsufficientFundsException e) {
            transactionLogService.updateStatus(transactionLog,TransactionStatus.FAILED);

            throw  e;
        } catch (Exception ex){
            transactionLogService.updateStatus(transactionLog,TransactionStatus.FAILED);
            throw ex;
        }


    }

    @Transactional
    public TransactionResponseDto withdrawMoney(String idempotencyKey,WithdrawRequestDto withdrawRequest) {

        Optional<ResponseEntity<TransactionResponseDto>> stored =
                idempotencyService.getStoredResponse(idempotencyKey,TransactionResponseDto.class);
        if (stored.isPresent()){
            return stored.get().getBody();
        }

        //This function is for withdrawing like from atm so no ownership checking
        Account account = accountRepository.findByAccountNumber(withdrawRequest.accountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");


        }
        Account bankAccount = accountRepository.findByAccountNumber("BANK-INTERNAL")
                .orElseThrow(() -> new ResourceNotFoundException("bank internal account not found"));

        String transactionRef = referenceGenerator.generate();


        TransactionLog transactionLog = transactionLogService.savePending(
                TransactionLog.builder()
                        .reference(transactionRef)
                        .amount(withdrawRequest.amount())
                        .status(TransactionStatus.PENDING)
                        .description(withdrawRequest.note())
                        .fromAccount(account)
                        .toAccount(bankAccount)
                        .transactionType(TransactionType.WITHDRAW)
                        .build()


        );


        try {

            BigDecimal balance = calculateAccountBalance(account);
            if (balance.compareTo(withdrawRequest.amount()) < 0) {
                throw new InsufficientFundsException("Not enough funds");
            }
            LedgerEntry accountDebit = LedgerEntry.builder()
                    .account(account)
                    .amount(withdrawRequest.amount())
                    .transactionLog(transactionLog)
                    .entryType(EntryType.DEBIT)
                    .description(withdrawRequest.note())
                    .build();

            LedgerEntry bankCredit = LedgerEntry.builder()
                    .account(bankAccount)
                    .amount(withdrawRequest.amount())
                    .transactionLog(transactionLog)
                    .entryType(EntryType.CREDIT)
                    .description(withdrawRequest.note())
                    .build();

            ledgerEntryRepository.save(accountDebit);
            ledgerEntryRepository.save(bankCredit);

            transactionLog.setStatus(TransactionStatus.COMPLETED);
            transactionLogRepository.save(transactionLog);


            BigDecimal newBalance = calculateAccountBalance(account);

            TransactionResponseDto response = new TransactionResponseDto(
                    account.getAccountNumber(),
                    transactionRef,
                    TransactionType.WITHDRAW,
                    withdrawRequest.amount(),
                    account.getCurrency(),
                    newBalance,
                    LocalDateTime.now()
            );

            idempotencyService.store(idempotencyKey, response, 201);

            return response;




        } catch (Exception e) {
            transactionLogService.updateStatus(transactionLog, TransactionStatus.FAILED);
            throw e;

        }


    }

    @Transactional(readOnly = true)
    public Page<TransactionHistoryDto> getTransactionHistory(String email, String accountNumber, Pageable pageable) {

        Account myAccount = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("The Account you want to send money from is not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!accountRepository.existsByAccountNumberAndUser(myAccount.getAccountNumber(), user)) {
            throw new AccessDeniedException("You do not own the account you are trying to send money from");

        }

        Page<TransactionLog> logs = transactionLogRepository.findByFromAccountOrToAccount(myAccount,myAccount, pageable);

        return logs.map(log -> mapToDto(log,myAccount));




    }

    private TransactionHistoryDto mapToDto(TransactionLog log, Account myAccount) {
        String fromAccount = log.getFromAccount().getAccountNumber();
        String toAccount = log.getToAccount().getAccountNumber();
        String direction = null;

        if (log.getTransactionType() == TransactionType.WITHDRAW ||
                log.getTransactionType() == TransactionType.DEPOSIT){
            fromAccount = null;
            toAccount = null;

        }else{
            direction = log.getToAccount().getAccountNumber()
                    .equals(myAccount.getAccountNumber()) ? "IN" : "OUT";
        }

        return new TransactionHistoryDto(
                log.getReference(),
                log.getTransactionType(),
                log.getStatus(),
                log.getAmount(),
                myAccount.getCurrency(),
                fromAccount,
                toAccount,
                direction,
                log.getDescription(),
                log.getCreatedAt()
        );
    }




    }
