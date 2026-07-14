package org.example.dtos.UserDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.example.dtos.AddressDtos.AddressRequestDto;
import org.example.entities.Role;

import java.util.List;

public record UserRequestDto(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstname,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastname,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,

        @NotBlank(message = "Password Is Required")
        @Size(min = 5, max = 120, message = "Password must be between 5 and 120 characters")
        String password,

        @NotEmpty(message = "At least one address is required")
        @Valid
        List<AddressRequestDto> addresses,

        Role role
) {
}