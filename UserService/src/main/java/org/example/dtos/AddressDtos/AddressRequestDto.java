package org.example.dtos.AddressDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequestDto(
        @NotBlank(message = "Country is required")
        @Size(max = 255, message = "Country name is too long")
        String country,

        @NotBlank(message = "State is required")
        @Size(max = 255, message = "State name is too long")
        String state,

        @NotBlank(message = "City is required")
        @Size(max = 255, message = "City name is too long")
        String city,

        @NotBlank(message = "Street is required")
        @Size(max = 255, message = "Street name is too long")
        String street,

        @NotBlank(message = "House number is required")
        @Size(max = 20, message = "House number is too long")
        String houseNumber,

        @NotBlank(message = "Zipcode is required")
        @Size(min = 3, max = 20, message = "Zipcode must be between 3 and 20 characters")
        String zipcode
) {
}