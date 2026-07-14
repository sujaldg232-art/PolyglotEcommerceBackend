package org.example.dtos.UserDto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequestDto(

        @NotBlank(message = "Password Is Required")
        @Size(min = 5, max = 120)
        @Column(nullable = false, length = 120)
        String newPassword,

        String oldPassword
) {
}
