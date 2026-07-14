package org.example.service.UserService;

import org.example.dtos.AddressDtos.AddressRequestDto;
import org.example.dtos.UserDto.UserRequestDto;
import org.example.dtos.UserDto.UserResponseDto;
import org.example.entities.Address;
import org.example.entities.UserData;
import org.example.mapper.UserMapper;
import org.example.repos.UserDataRepo;
import org.example.utilities.JwtUtil;
import org.example.utilities.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDataRepo userDataRepo;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private S3Service s3Service;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @InjectMocks
    private UserService userService;


     UserData userData;
     UserRequestDto userRequestDto;
     UserResponseDto userResponseDto;
     UUID userId;

     @BeforeEach
     void setUp(){
         userId = UUID.randomUUID();

         Address address = Address.builder()
                 .id(UUID.randomUUID())
                 .country("Japan")
                 .state("Tokyo")
                 .city("Tokyo")
                 .street("Minato")
                 .houseNumber("1-1-1")
                 .zipcode("105-0011")
                 .build();

         userData = UserData.builder()
                 .id(userId)
                 .profilePictureUrl("default.jpg")
                 .firstname("DIO")
                 .lastname("BRANDO")
                 .email("dio1@gmail.com")
                 .password("theWorld")
                 .addresses(java.util.List.of(address))
                 .role(org.example.entities.Role.BOTH)
                 .isActive(true)
                 .isDeleted(false)
                 .build();

         address.setUser(userData);

         AddressRequestDto addressRequestDto = new AddressRequestDto(
                 "Japan", "Tokyo", "Tokyo", "Minato", "1-1-1", "105-0011"
         );

         userRequestDto = new UserRequestDto(
                 "DIO",
                 "BRANDO",
                 "dio1@gmail.com",
                 "theWorld",
                 java.util.List.of(addressRequestDto),
                 org.example.entities.Role.BOTH
         );

         userResponseDto = UserResponseDto.builder()
                 .id(userId)
                 .profilePictureUrl("default.jpg")
                 .firstname("DIO")
                 .lastname("BRANDO")
                 .email("dio1@gmail.com")
                 .addresses(java.util.Collections.emptyList())
                 .role(org.example.entities.Role.BOTH)
                 .build();

     }


     @Nested
     class LoginUser{

     }

    @Nested
    class RegisterUser{

        @Test
        void RegTestWhenSuccess(){
            String expectedHashedPassword = "hashedPassword123";

            when(passwordHasher.hash(userRequestDto.password())).thenReturn(expectedHashedPassword);
            when(userMapper.userRegtoEntity(userRequestDto)).thenReturn(userData);
            when(userDataRepo.save(userData)).thenReturn(userData);
            when(userMapper.toResponseDto(userData)).thenReturn(userResponseDto);

            UserResponseDto result = userService.userRegistration(userRequestDto);

            assertNotNull(result);
            assertEquals(userResponseDto, result);
            assertEquals("dio1@gmail.com", userData.getEmail());
            assertEquals(expectedHashedPassword, userData.getPassword());

            org.mockito.Mockito.verify(passwordHasher, org.mockito.Mockito.times(1)).hash(userRequestDto.password());
            org.mockito.Mockito.verify(userMapper, org.mockito.Mockito.times(1)).userRegtoEntity(userRequestDto);
            org.mockito.Mockito.verify(userDataRepo, org.mockito.Mockito.times(1)).save(userData);
            org.mockito.Mockito.verify(userMapper, org.mockito.Mockito.times(1)).toResponseDto(userData);
        }

        @Test
        void userRegistrationTestShouldTrimAndLowercaseEmail() {
            userRequestDto = new UserRequestDto(
                    "DIO", "BRANDO", "   DiO1@GmAil.CoM   ", "theWorld",
                    java.util.List.of(), org.example.entities.Role.BOTH
            );
            String expectedHashedPassword = "hashedPassword123";

            when(passwordHasher.hash(anyString())).thenReturn(expectedHashedPassword);
            when(userMapper.userRegtoEntity(userRequestDto)).thenReturn(userData);
            when(userDataRepo.save(userData)).thenReturn(userData);
            when(userMapper.toResponseDto(userData)).thenReturn(userResponseDto);

            userService.userRegistration(userRequestDto);

            org.junit.jupiter.api.Assertions.assertEquals("dio1@gmail.com", userData.getEmail());
        }

        @Test
        void userRegistrationTestShouldThrowExceptionWhenDatabaseSaveFails() {
            when(passwordHasher.hash(anyString())).thenReturn("hashed");
            when(userMapper.userRegtoEntity(userRequestDto)).thenReturn(userData);
            when(userDataRepo.save(userData)).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Email already exists"));

            org.junit.jupiter.api.Assertions.assertThrows(
                    org.springframework.dao.DataIntegrityViolationException.class,
                    () -> userService.userRegistration(userRequestDto)
            );

            org.mockito.Mockito.verify(userMapper, org.mockito.Mockito.never()).toResponseDto(any());
        }
    }
}