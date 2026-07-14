package org.example.mapper;

import org.example.dtos.UserDto.UserRequestDto;
import org.example.dtos.UserDto.UserResponseDto;
import org.example.entities.UserData;
import org.example.service.UserService.S3Service;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public abstract class UserMapper {

    @Value("${cloud.aws.s3.pfpbucket}")
    private String profileBucket;

    @Autowired
    protected S3Service s3Service;

    public abstract UserResponseDto toResponseDto(UserData userData);

    public abstract UserData userRegtoEntity(UserRequestDto dto);

    @AfterMapping
    protected void linkAddresses(@MappingTarget UserData userData) {
        if (userData.getAddresses() != null) {
            userData.getAddresses().forEach(address -> address.setUser(userData));
        }
    }

    @AfterMapping
    protected void convertKeyToPresignedUrl(UserData userData, @MappingTarget UserResponseDto.UserResponseDtoBuilder dtoBuilder) {
        if (userData.getProfilePictureUrl() != null && !userData.getProfilePictureUrl().isEmpty()) {
            String presignedUrl = s3Service.createPresignedGetUrl(profileBucket, userData.getProfilePictureUrl());
            dtoBuilder.profilePictureUrl(presignedUrl);
        }
    }
}