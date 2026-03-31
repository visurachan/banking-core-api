package com.banking.banking_api.auth;

import com.banking.banking_api.auth.dto.RegisterRequestDto;
import com.banking.banking_api.auth.dto.RegisterResponseDto;
import com.banking.banking_api.common.exception.DuplicateResourceException;
import com.banking.banking_api.user.User;
import com.banking.banking_api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

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


}
