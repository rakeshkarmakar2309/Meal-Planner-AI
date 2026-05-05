package com.Meal_Planner.AI.graphql;

import com.Meal_Planner.AI.dto.request.LoginRequest;
import com.Meal_Planner.AI.dto.request.RefreshTokenRequest;
import com.Meal_Planner.AI.dto.request.RegisterRequest;
import com.Meal_Planner.AI.dto.response.AuthResponse;
import com.Meal_Planner.AI.dto.response.UserResponse;
import com.Meal_Planner.AI.entity.User;
import com.Meal_Planner.AI.exception.BadRequestException;
import com.Meal_Planner.AI.graphql.input.LoginInput;
import com.Meal_Planner.AI.graphql.input.RegisterInput;
import com.Meal_Planner.AI.repository.UserRepository;
import com.Meal_Planner.AI.security.UserPrincipal;
import com.Meal_Planner.AI.service.AuthService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@DgsComponent
@RequiredArgsConstructor
public class AuthResolver {

    private final AuthService authService;
    private final UserRepository userRepository;

    // ── Public mutations ──────────────────────────────────────────────────────

    @DgsMutation
    public AuthResponse register(@InputArgument RegisterInput input) {
        return authService.register(
                new RegisterRequest(input.email(), input.password(), input.name())
        );
    }

    @DgsMutation
    public AuthResponse login(@InputArgument LoginInput input) {
        return authService.login(
                new LoginRequest(input.email(), input.password())
        );
    }

    @DgsMutation
    public AuthResponse refresh(@InputArgument String refreshToken) {
        return authService.refresh(new RefreshTokenRequest(refreshToken));
    }

    // ── Protected mutations ───────────────────────────────────────────────────

    @DgsMutation
    public boolean logout() {
        // Use helper to enforce authentication and get the User entity
        authService.logout(getAuthenticatedUser());
        return true;
    }

    // ── Protected queries ─────────────────────────────────────────────────────

    @DgsQuery
    public UserResponse me() {
        User user = getAuthenticatedUser();
        return new UserResponse(
                user.getId().toString(),
                user.getEmail(),
                user.getName()
        );
    }

    // ── Helper Methods ────────────────────────────────────────────────────────

    /**
     * Centralized helper to enforce security.
     * It checks if the JwtAuthFilter successfully populated the SecurityContext.
     */
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verify that the filter actually found and validated a token
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Access denied — valid Bearer token required");
        }

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        // Return the full User entity for service layer usage
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("Authenticated user no longer exists in database"));
    }
}