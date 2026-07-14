package org.example.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Country is required")
    @Size(max = 255, message = "Countru name is too long")
    @Column(nullable = false)
    private String country;

    @NotBlank(message = "State is required")
    @Size(max = 255, message = "State name is too long")
    @Column(nullable = false)
    private String state;


    @NotBlank(message = "City is required")
    @Size(max = 255, message = "City name is too long")
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street name is too long")
    @Column(nullable = false)
    private String street;

    @NotBlank(message = "House number is required")
    @Size(max = 20, message = "House number is too long")
    @Column(nullable = false)
    private String houseNumber;

    @NotBlank(message = "Zipcode is required")
    @Size(min = 3, max = 20, message = "Zipcode must be between 3 and 20 characters")
    @Column(length = 20, nullable = false)
    private String zipcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserData user;
}
