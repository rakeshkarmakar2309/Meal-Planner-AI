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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

@DgsComponent          // DGS equivalent of @Controller — registers this as a resolver
@RequiredArgsConstructor
public class AuthResolver {

    private final AuthService authService;
    private final UserRepository userRepository;

    // ── Public mutations ──────────────────────────────────────────────────────

    @DgsMutation       // maps to "register" in your schema's Mutation type
    public AuthResponse register(
            @InputArgument RegisterInput input  // DGS deserializes JSON → RegisterInput
    ) {
        // Translate graphql input → service layer DTO
        // Keeps your service completely unaware of GraphQL
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
    // Single scalar arg — no @InputArgument wrapper type needed
    // "refreshToken" must match the argument name in schema: refresh(refreshToken: String!)
    public AuthResponse refresh(@InputArgument String refreshToken) {
        return authService.refresh(new RefreshTokenRequest(refreshToken));
    }

    // ── Protected mutations ───────────────────────────────────────────────────

    @DgsMutation
    public boolean logout(@AuthenticationPrincipal UserPrincipal principal) {
        // JwtAuthFilter already ran — if token was valid, principal is set
        // If no token or invalid token, principal is null → we throw
        User user = requireAuthenticated(principal, "logout");
        authService.logout(user);
        return true;
    }

    // ── Protected queries ─────────────────────────────────────────────────────

    @DgsQuery
    public UserResponse me() {
        // Manually pull from the SecurityContext where your JwtAuthFilter saved it
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new AccessDeniedException("Access denied — provide a valid Bearer token");
        }

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("Authenticated user not found"));

        return new UserResponse(user.getId().toString(), user.getEmail(), user.getName());
    }

    // ─────────────────────────────────────────────────────────────────────────

    private User requireAuthenticated(UserPrincipal principal, String operation) {
        if (principal == null) {
            // Becomes errors[].extensions.classification = "PERMISSION_DENIED"
            // HTTP status is still 200 — this error lives in the response body
            throw new AccessDeniedException("Login required to perform: " + operation);
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new BadRequestException("Authenticated user not found"));
    }
}