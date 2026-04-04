package com.banking.banking_api.ledger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionLogService {

    private final TransactionLogRepository transactionLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransactionLog savePending(TransactionLog log) {
        return transactionLogRepository.save(log);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(TransactionLog log, TransactionStatus status) {
        log.setStatus(status);
        transactionLogRepository.save(log);
    }
}