package com.Meal_Planner.AI.exception;

import com.netflix.graphql.dgs.exceptions.DefaultDataFetcherExceptionHandler;
import com.netflix.graphql.types.errors.ErrorType;
import com.netflix.graphql.types.errors.TypedGraphQLError;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

    // Fall through to DGS default for anything we don't handle explicitly
    private final DefaultDataFetcherExceptionHandler defaultHandler =
            new DefaultDataFetcherExceptionHandler();

    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(
            DataFetcherExceptionHandlerParameters params
    ) {
        Throwable exception = params.getException();

        if (exception instanceof BadRequestException) {
            var error = TypedGraphQLError.newBadRequestBuilder()
                    .message(exception.getMessage())
                    .path(params.getPath())
                    .build();
            return CompletableFuture.completedFuture(
                    DataFetcherExceptionHandlerResult.newResult(error).build()
            );
        }

        if (exception instanceof ResourceNotFoundException) {
            var error = TypedGraphQLError.newBuilder()
                    .errorType(ErrorType.NOT_FOUND)
                    .message(exception.getMessage())
                    .path(params.getPath())
                    .build();
            return CompletableFuture.completedFuture(
                    DataFetcherExceptionHandlerResult.newResult(error).build()
            );
        }

        if (exception instanceof AccessDeniedException) {
            var error = TypedGraphQLError.newPermissionDeniedBuilder()
                    .message("Access denied — provide a valid Bearer token")
                    .path(params.getPath())
                    .build();
            return CompletableFuture.completedFuture(
                    DataFetcherExceptionHandlerResult.newResult(error).build()
            );
        }

        if (exception instanceof AuthenticationException) {
            var error = TypedGraphQLError.newBuilder()
                    .errorType(ErrorType.UNAUTHENTICATED)
                    // Generic on purpose — never hint which field (email vs password) is wrong
                    .message("Invalid email or password")
                    .path(params.getPath())
                    .build();
            return CompletableFuture.completedFuture(
                    DataFetcherExceptionHandlerResult.newResult(error).build()
            );
        }

        if (exception instanceof ConstraintViolationException cve) {
            String message = cve.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            var error = TypedGraphQLError.newBadRequestBuilder()
                    .message(message)
                    .path(params.getPath())
                    .build();
            return CompletableFuture.completedFuture(
                    DataFetcherExceptionHandlerResult.newResult(error).build());

        }

        log.error("Unhandled GraphQL exception at {}", params.getPath(), exception);
        return defaultHandler.handleException(params);
    }
}