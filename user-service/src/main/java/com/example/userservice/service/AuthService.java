package com.example.userservice.service;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.RefreshTokenRequest;
import com.example.userservice.dto.RefreshTokenResponse;
import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.RegisterResponse;
import com.example.userservice.event.UserRegisteredEvent;
import com.example.userservice.exception.DuplicateResourceException;
import com.example.userservice.exception.UnauthorizedException;
import com.example.userservice.model.RefreshToken;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.jwt.JwtService;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserEventProducer userEventProducer;
    private final long accessTokenExpirationMs;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        RefreshTokenService refreshTokenService,
        UserEventProducer userEventProducer,
        @Value(
            "${app.jwt.access-token-expiration-ms}"
        ) long accessTokenExpirationMs
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userEventProducer = userEventProducer;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = new User(
            request.getUsername(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword())
        );
        User saved = userRepository.save(user);

        UserRegisteredEvent event = new UserRegisteredEvent(
            "USER_REGISTERED",
            saved.getId(),
            saved.getUsername(),
            saved.getEmail(),
            Instant.now()
        );
        userEventProducer.publishUserRegistered(event);

        return new RegisterResponse(
            saved.getId(),
            saved.getUsername(),
            saved.getEmail(),
            saved.getCreatedAt()
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        User user = userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() ->
                new UnauthorizedException("Invalid credentials")
            );

        String accessToken = jwtService.generateAccessToken(
            user.getId(),
            user.getUsername()
        );
        refreshTokenService.revokeAllForUser(user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
            user
        );

        return new AuthResponse(
            accessToken,
            refreshToken.getToken(),
            "Bearer",
            accessTokenExpirationMs / 1000
        );
    }

    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken rotated = refreshTokenService.rotateRefreshToken(
            request.getRefreshToken()
        );
        User user = rotated.getUser();

        String accessToken = jwtService.generateAccessToken(
            user.getId(),
            user.getUsername()
        );
        return new RefreshTokenResponse(
            accessToken,
            rotated.getToken(),
            accessTokenExpirationMs / 1000
        );
    }
}
