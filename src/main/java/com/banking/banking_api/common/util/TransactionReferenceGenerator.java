package com.banking.banking_api.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionReferenceGenerator {

    private final JdbcTemplate jdbcTemplate;

    public String generate() {
        Long nextVal = jdbcTemplate.queryForObject(
                "SELECT nextval('transaction_reference_seq')", Long.class
        );
        return String.format("TXN-%06d", nextVal);
    }
}
