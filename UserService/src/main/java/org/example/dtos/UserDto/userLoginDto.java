package org.example.dtos.UserDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record userLoginDto(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 32,min = 5) String password
) {
}
