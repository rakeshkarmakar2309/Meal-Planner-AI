package com.Meal_Planner.AI.graphql;

import com.Meal_Planner.AI.dto.request.LoginRequest;
import com.Meal_Planner.AI.dto.request.RefreshTokenRequest;
import com.Meal_Planner.AI.dto.request.RegisterRequest;
import com.Meal_Planner.AI.dto.response.AuthResponse;
import com.Meal_Planner.AI.dto.response.UserResponse;
import com.Meal_Planner.AI.entity.User;
import com.Meal_Planner.AI.graphql.input.LoginInput;
import com.Meal_Planner.AI.graphql.input.RegisterInput;
import com.Meal_Planner.AI.service.AuthService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
@RequiredArgsConstructor
public class AuthResolver {

    private final AuthService authService;

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
    @PreAuthorize("isAuthenticated()")
    public boolean logout() {
        // Use helper to enforce authentication and get the User entity
        authService.logout();
        return true;
    }

    // ── Protected queries ─────────────────────────────────────────────────────

    @DgsQuery
    @PreAuthorize("isAuthenticated()")
    public UserResponse me() {
        return authService.toUserResponse(authService.getAuthenticatedUser());
    }
}