package com.banking.banking_api.account;

import com.banking.banking_api.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUser (User user);
    Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumberAndUser(String accountNumber, User user);


}
