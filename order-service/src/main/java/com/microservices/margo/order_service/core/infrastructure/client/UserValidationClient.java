package com.microservices.margo.order_service.core.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidationClient {

    private final RestClient restClient;

    @Value("${user-service.users-url}")
    private String usersUrl;

    public void validateUserExists(UUID userId) {
        try {
            restClient.get()
                    .uri(usersUrl + userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new IllegalArgumentException("User not found: " + userId);
                    })
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            log.error("Users service unreachable", e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Users service is unavailable");
        }
    }
}