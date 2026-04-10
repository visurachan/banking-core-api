package com.banking.banking_api.common.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public <T> void store(String key, T response, int httpStatus) {
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            idempotencyKeyRepository.save(IdempotencyKey.builder()
                    .idempotencyKey(key)
                    .response(responseJson)
                    .httpStatus(httpStatus)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize response", e);
        }
    }

    public <T> Optional<ResponseEntity<T>> getStoredResponse(String key, Class<T> responseType) {
        return idempotencyKeyRepository.findByIdempotencyKey(key)
                .filter(k -> k.getCreatedAt().isAfter(LocalDateTime.now().minusHours(24)))
                .map(k -> {
                    try {
                        T response = objectMapper.readValue(k.getResponse(), responseType);
                        return ResponseEntity.status(k.getHttpStatus()).body(response);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to deserialize response", e);
                    }
                });
    }
}