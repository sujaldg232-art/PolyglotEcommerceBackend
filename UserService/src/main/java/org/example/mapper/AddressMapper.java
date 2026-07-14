package org.example.mapper;

import org.example.dtos.AddressDtos.AddressRequestDto;
import org.example.dtos.AddressDtos.AddressResponseDto;
import org.example.entities.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address requeesttoEntity(AddressRequestDto addressRequestDto);

    AddressResponseDto toResponseDto(Address entity);

    Address requestToDto(Address address);
}