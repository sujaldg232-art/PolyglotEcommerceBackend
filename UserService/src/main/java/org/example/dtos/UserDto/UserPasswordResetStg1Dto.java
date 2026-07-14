package org.example.dtos.UserDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserPasswordResetStg1Dto (
        @NotBlank
        @Email
        String email
){
}