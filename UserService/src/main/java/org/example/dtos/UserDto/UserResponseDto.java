package org.example.dtos.UserDto;

import lombok.Builder;
import org.example.dtos.AddressDtos.AddressResponseDto;
import org.example.entities.Role;

import java.util.List;
import java.util.UUID;

@Builder
public record UserResponseDto(
        UUID id,
        String profilePictureUrl,
        String firstname,
        String lastname,
        String email,
        List<AddressResponseDto> addresses,
        Role role
) {
}