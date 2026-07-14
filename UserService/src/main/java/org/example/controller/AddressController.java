package org.example.controller;

import jakarta.validation.Valid;
import org.example.dtos.AddressDtos.AddressRequestDto;
import org.example.dtos.AddressDtos.AddressResponseDto;
import org.example.entities.Address;
import org.example.entities.UserData;
import org.example.mapper.AddressMapper;
import org.example.repos.UserDataRepo;
import org.example.service.AddressService.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/Address")
public class AddressController {

    AddressService addressService;
    AddressMapper addressMapper;
    UserDataRepo userDataRepo;


    @Autowired
    public AddressController(AddressService addressService,AddressMapper addressMapper,UserDataRepo  userDataRepo){
        this.userDataRepo = userDataRepo;
        this.addressService = addressService;
        this.addressMapper = addressMapper;
    }

    @PostMapping("/addNew")
    public ResponseEntity<AddressResponseDto> addNewAddress(
                @RequestHeader("X-User-Id") String userIdStr,
            @RequestBody @Valid AddressRequestDto addressRequestDto

    ){
        UUID userUUID = UUID.fromString(userIdStr);

        UserData  user = userDataRepo.findById(userUUID).orElse(null);

        if(user == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + userUUID);
        }

        return ResponseEntity.ok(addressService.addNewAddress(user,addressRequestDto));
    }

    @PutMapping("/ChangeAddress")
    public ResponseEntity<AddressResponseDto> addressResponseDtoResponseEntity(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestParam UUID addressId,
            @RequestBody AddressRequestDto addressRequestDto
    ){
        UUID userID = UUID.fromString(userIdStr);
        UserData userData = userDataRepo.findById(userID).orElse(null);

        Address address = addressService.findById(addressId);

        if(userData == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Your User ID is Not Valid");
        }

        if(address == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Your Address ID is Not Valid");
        }

        if (address.getUser() == null || !address.getUser().getId().equals(userData.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return  ResponseEntity.ok(addressService.change(addressRequestDto,address));
    }

}
