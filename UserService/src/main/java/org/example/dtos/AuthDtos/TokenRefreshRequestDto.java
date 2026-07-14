package org.example.dtos.AuthDtos;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequestDto(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}