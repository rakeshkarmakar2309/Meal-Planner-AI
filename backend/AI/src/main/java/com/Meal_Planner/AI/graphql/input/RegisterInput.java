package com.Meal_Planner.AI.graphql.input;

public record RegisterInput(
        String email,
        String password,
        String name
) {}