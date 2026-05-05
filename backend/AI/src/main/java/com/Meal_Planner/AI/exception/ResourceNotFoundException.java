package com.Meal_Planner.AI.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(String entity, Object id) {
        super(entity + " not found with id: " + id);
    }
}