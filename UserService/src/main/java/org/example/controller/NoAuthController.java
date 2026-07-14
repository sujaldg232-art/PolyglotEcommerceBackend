package org.example.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.example.dtos.AuthDtos.TokenRefreshRequestDto;
import org.example.dtos.AuthDtos.TokenRefreshResponseDto;
import org.example.dtos.UserDto.*;
import org.example.entities.RefreshToken;
import org.example.entities.UserData;
import org.example.service.UserService.EmailService;
import org.example.service.AuthService.RefreshTokenService;
import org.example.service.UserService.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping()
public class NoAuthController {

    private final UserService userService;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public NoAuthController(UserService userService, EmailService emailService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/Registration")
    public ResponseEntity<UserResponseDto> userRegistration(@RequestBody @Valid UserRequestDto userRequestDto) {
        UserResponseDto response = userService.userRegistration(userRequestDto);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.badRequest().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> userLogin(@RequestBody @Valid userLoginDto userRequest) {
        String email = userRequest.email().toLowerCase().trim();
        String password = userRequest.password();

        UserData user = userService.findByEmail(email);

        refreshTokenService.deleteByUserDat(user);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        if (!user.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account is Deactivated");
        }

        if (user.isDeleted()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account is pending deletion");
        }

        String accessToken = userService.jwtGenerationHandler(email, password);
        if (accessToken != null) {
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
            return ResponseEntity.ok(new TokenRefreshResponseDto(accessToken, refreshToken.getToken()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody @Valid TokenRefreshRequestDto request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(token -> {
                    UserData user = token.getUserDat();
                    if (!user.isActive() || user.isDeleted()) {
                        refreshTokenService.deleteByUserId(user.getId());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body("Account status invalid. Access denied.");
                    }
                    String newAccessToken = userService.generateAccessTokenDirectly(user.getId());
                    return ResponseEntity.ok(new TokenRefreshResponseDto(newAccessToken, token.getToken()));
                }).orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }


    @PostMapping("/PasswordResetstg1")
    public ResponseEntity passwordResetOtp(@RequestBody @Valid UserPasswordResetStg1Dto userRequest) {
        UserData user = userService.findByEmail(userRequest.email());

        if(user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Your User Email is Not Valid");

        EmailService.ResultOfOtp result = emailService.sendPasswordResetEmail(user.getEmail());

        userService.saveOtp(user.getEmail(), result.getOtp());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/PasswordResetstg2")
    public ResponseEntity<Map<String, String>> passwordResetOtpConfirmation(@RequestBody @Valid UserPasswordResetStg2Dto userRequest) {
        String token = userService.verifyOtp(userRequest.email(), userRequest.otp());
        if (token == null) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(Map.of("resetToken", token));
        }
    }

    @PostMapping("/PasswordResetstg3")
    public ResponseEntity passowrdChange(@RequestBody @Valid UserPasswordResetStg3Dto userRequest) {
        boolean success = userService.resetPassword(
                userRequest.email(),
                userRequest.resetToken(),
                userRequest.newPassword()
        );
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}