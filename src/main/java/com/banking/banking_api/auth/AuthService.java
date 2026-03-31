package com.banking.banking_api.auth;

import com.banking.banking_api.auth.dto.LoginRequestDto;
import com.banking.banking_api.auth.dto.LoginResponseDto;
import com.banking.banking_api.auth.dto.RegisterRequestDto;
import com.banking.banking_api.auth.dto.RegisterResponseDto;
import com.banking.banking_api.common.exception.DuplicateResourceException;
import com.banking.banking_api.common.exception.ResourceNotFoundException;
import com.banking.banking_api.security.JwtTokenProvider;
import com.banking.banking_api.user.User;
import com.banking.banking_api.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public RegisterResponseDto registerNewUser(RegisterRequestDto request) {

        if (userRepository.existsByEmail(request.email())){
            throw new DuplicateResourceException("A user with email " +  request.email() + " already exists");

        }

        User user = User.builder()
                .email(request.email())
                .password(encoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .build();

        User savedUser= userRepository.save(user);

        return new RegisterResponseDto(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getRole(),
                savedUser.getCreatedAt()

        );

    }


    public LoginResponseDto loginUser(LoginRequestDto request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with email "+ request.email() + " not found"
                ));

        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getRole().name()
        );
        return new LoginResponseDto(token);






    }
}
