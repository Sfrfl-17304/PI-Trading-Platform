package com.example.userservice.service;

import com.example.userservice.dto.BalanceRequest;
import com.example.userservice.dto.UpdateProfileRequest;
import com.example.userservice.dto.UserProfileResponse;
import com.example.userservice.exception.BadRequestException;
import com.example.userservice.exception.DuplicateResourceException;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.UserPrincipal;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User user = getCurrentUserEntity();
        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateCurrentUser(UpdateProfileRequest request) {
        User user = getCurrentUserEntity();

        if (request.getEmail() != null) {
            String trimmedEmail = request.getEmail().trim();
            if (
                !trimmedEmail.isBlank() &&
                !trimmedEmail.equalsIgnoreCase(user.getEmail())
            ) {
                if (userRepository.existsByEmail(trimmedEmail)) {
                    throw new DuplicateResourceException(
                        "Email already exists"
                    );
                }
                user.setEmail(trimmedEmail);
            }
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    @Transactional
    public BigDecimal adjustBalance(BalanceRequest request) {
        User user = getCurrentUserEntity();
        BigDecimal amount = request.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be positive");
        }

        BigDecimal newBalance;
        if (request.getType() == BalanceRequest.Type.DEPOSIT) {
            newBalance = user.getBalance().add(amount);
        } else if (request.getType() == BalanceRequest.Type.WITHDRAW) {
            if (user.getBalance().compareTo(amount) < 0) {
                throw new BadRequestException("Insufficient balance");
            }
            newBalance = user.getBalance().subtract(amount);
        } else {
            throw new BadRequestException("Invalid balance operation");
        }

        user.setBalance(newBalance);
        userRepository.save(user);
        return newBalance;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(UUID userId) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toProfileResponse(user);
    }

    private User getCurrentUserEntity() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (
            authentication == null ||
            !(authentication.getPrincipal() instanceof UserPrincipal principal)
        ) {
            throw new BadRequestException("Invalid authentication context");
        }
        UUID userId = principal.getId();
        return userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getBalance(),
            user.getCreatedAt()
        );
    }
}
