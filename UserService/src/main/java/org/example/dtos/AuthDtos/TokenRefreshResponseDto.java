package org.example.dtos.AuthDtos;

public record TokenRefreshResponseDto(
        String accessToken,
        String refreshToken
) {}