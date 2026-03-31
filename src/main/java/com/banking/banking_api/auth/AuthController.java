package com.banking.banking_api.auth;

import com.banking.banking_api.auth.dto.RegisterRequestDto;
import com.banking.banking_api.auth.dto.RegisterResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication APIs", description = "Operations related to authentication")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> registerNewUser(
            @Valid @RequestBody RegisterRequestDto request
            ) {
        RegisterResponseDto response = authService.registerNewUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
