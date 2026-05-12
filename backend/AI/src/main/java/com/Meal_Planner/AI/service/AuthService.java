package com.Meal_Planner.AI.service;

import com.Meal_Planner.AI.dto.request.LoginRequest;
import com.Meal_Planner.AI.dto.request.RefreshTokenRequest;
import com.Meal_Planner.AI.dto.request.RegisterRequest;
import com.Meal_Planner.AI.dto.response.AuthResponse;
import com.Meal_Planner.AI.dto.response.UserResponse;
import com.Meal_Planner.AI.entity.RefreshToken;
import com.Meal_Planner.AI.entity.User;
import com.Meal_Planner.AI.exception.BadRequestException;
import com.Meal_Planner.AI.exception.ResourceNotFoundException;
import com.Meal_Planner.AI.repository.RefreshTokenRepository;
import com.Meal_Planner.AI.repository.UserRepository;
import com.Meal_Planner.AI.security.JwtUtil;
import com.Meal_Planner.AI.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse register(@Valid RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Delegates to Spring Security — throws BadCredentialsException on failure
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", principal.getId()));

        refreshTokenRepository.deleteByUser(user);
        return buildAuthResponse(user);

    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new BadRequestException("Refresh token expired — please login again");
        }

        User user = stored.getUser();
        refreshTokenRepository.delete(stored);
        return buildAuthResponse(user);
    }

    @Transactional
    public void logout() {
        User user=getAuthenticatedUser();
        refreshTokenRepository.deleteByUser(user);
    }

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Valid Bearer token required");
        }
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException(
                        "Authenticated user no longer exists"));
    }


    // ── Private helpers ───────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, toUserResponse(user));
    }

    private String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .user(user)
                        .token(token)
                        .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                        .build()
        );
        return token;
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getName()
        );
    }


}
