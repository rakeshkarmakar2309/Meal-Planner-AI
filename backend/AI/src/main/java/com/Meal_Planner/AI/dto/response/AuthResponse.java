package com.Meal_Planner.AI.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {}