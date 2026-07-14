package org.example.dtos.AddressDtos;

public record AddressResponseDto(
        String country,
        String state,
        String city,
        String street,
        String houseNumber,
        String zipcode
) {
}