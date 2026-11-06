package ru.rest.wallet.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}