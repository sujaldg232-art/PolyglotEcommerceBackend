package org.example.dtos.UserDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserPasswordResetStg2Dto (
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(regexp = "^\\d{6}$")
        String otp
){
}