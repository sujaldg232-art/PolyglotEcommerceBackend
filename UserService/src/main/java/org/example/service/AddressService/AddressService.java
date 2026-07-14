package org.example.service.AddressService;

import org.example.dtos.AddressDtos.AddressRequestDto;
import org.example.dtos.AddressDtos.AddressResponseDto;
import org.example.entities.Address;
import org.example.entities.UserData;
import org.example.mapper.AddressMapper;
import org.example.repos.AddressRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AddressService {

    AddressRepo addressRepo;
    AddressMapper addressMapper;

    @Autowired
    public AddressService(AddressRepo addressRepo,AddressMapper addressMapper){
        this.addressRepo = addressRepo;
        this.addressMapper = addressMapper;
    }

    public Address findById(UUID uuid){
        Address address = addressRepo.findById(uuid).orElse(null);

        if(address == null){
            throw new ResponseStatusException(HttpStatusCode.valueOf(400));
        }

        return address;
    }

    @Transactional
    public AddressResponseDto addNewAddress(UserData userData,AddressRequestDto addressRequestDto){
        Address address = addressMapper.requeesttoEntity(addressRequestDto);
        address.setUser(userData);

        if (userData.getAddresses() != null) {
            userData.getAddresses().add(address);
        }

        addressRepo.save(address);
        return addressMapper.toResponseDto(address);
    }

    @Transactional
    public AddressResponseDto change(AddressRequestDto addressRequestDto, Address address) {
        if (addressRequestDto.country() != null) {
            address.setCountry(addressRequestDto.country());
        }
        if (addressRequestDto.state() != null) {
            address.setState(addressRequestDto.state());
        }
        if (addressRequestDto.city() != null) {
            address.setCity(addressRequestDto.city());
        }
        if (addressRequestDto.street() != null) {
            address.setStreet(addressRequestDto.street());
        }
        if (addressRequestDto.houseNumber() != null) {
            address.setHouseNumber(addressRequestDto.houseNumber());
        }
        if (addressRequestDto.zipcode() != null) {
            address.setZipcode(addressRequestDto.zipcode());
        }

        addressRepo.save(address);

        return new AddressResponseDto(
                address.getCountry(),
                address.getState(),
                address.getCity(),
                address.getStreet(),
                address.getHouseNumber(),
                address.getZipcode()
        );
    }

}
