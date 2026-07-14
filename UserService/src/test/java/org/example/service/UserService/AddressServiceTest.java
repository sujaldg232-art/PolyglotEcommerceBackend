package org.example.service.UserService;

import org.example.dtos.AddressDtos.AddressRequestDto;
import org.example.dtos.AddressDtos.AddressResponseDto;
import org.example.entities.Address;
import org.example.entities.UserData;
import org.example.mapper.AddressMapper;
import org.example.repos.AddressRepo;
import org.example.service.AddressService.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepo addressRepo;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressService addressService;

    private Address address;
    private UserData userData;
    private AddressRequestDto requestDto;
    private AddressResponseDto responseDto;
    private UUID addressId;

    @BeforeEach
    void setUp() {
        addressId = UUID.randomUUID();

        userData = new UserData();
        userData.setAddresses(new ArrayList<>());

        address = Address.builder()
                .id(addressId)
                .country("US")
                .state("NY")
                .city("New York")
                .street("Broadway")
                .houseNumber("123")
                .zipcode("10001")
                .user(userData)
                .build();

        requestDto = new AddressRequestDto("US", "NY", "New York", "Broadway", "123", "10001");
        responseDto = new AddressResponseDto("US", "NY", "New York", "Broadway", "123", "10001");
    }

    @Test
    void findById_Success() {
        when(addressRepo.findById(addressId)).thenReturn(Optional.of(address));

        Address result = addressService.findById(addressId);

        assertNotNull(result);
        assertEquals(addressId, result.getId());
        verify(addressRepo, times(1)).findById(addressId);
    }

    @Test
    void findById_ThrowsExceptionWhenNotFound() {
        when(addressRepo.findById(addressId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            addressService.findById(addressId);
        });

        assertEquals(HttpStatusCode.valueOf(400), exception.getStatusCode());
        verify(addressRepo, times(1)).findById(addressId);
    }

    @Test
    void addNewAddress_Success() {
        when(addressMapper.requeesttoEntity(requestDto)).thenReturn(address);
        when(addressRepo.save(any(Address.class))).thenReturn(address);
        when(addressMapper.toResponseDto(address)).thenReturn(responseDto);

        AddressResponseDto result = addressService.addNewAddress(userData, requestDto);

        assertNotNull(result);
        assertEquals("US", result.country());
        assertTrue(userData.getAddresses().contains(address));
        verify(addressRepo, times(1)).save(address);
    }

    @Test
    void change_Success() {
        AddressRequestDto updateRequest = new AddressRequestDto("CA", "ON", "Toronto", "King St", "456", "M5V");
        when(addressRepo.save(any(Address.class))).thenReturn(address);

        AddressResponseDto result = addressService.change(updateRequest, address);

        assertNotNull(result);
        assertEquals("CA", result.country());
        assertEquals("ON", result.state());
        assertEquals("Toronto", result.city());
        assertEquals("King St", result.street());
        assertEquals("456", result.houseNumber());
        assertEquals("M5V", result.zipcode());
        verify(addressRepo, times(1)).save(address);
    }
}