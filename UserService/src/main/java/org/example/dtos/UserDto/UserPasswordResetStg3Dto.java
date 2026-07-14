package org.example.dtos.UserDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordResetStg3Dto (
        @NotBlank
        @Email
        String email,

        @NotBlank
        String resetToken,

        @NotBlank
        @Size(max = 32, min = 5)
        String newPassword
){
}
