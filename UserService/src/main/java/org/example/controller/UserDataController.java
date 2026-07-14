package org.example.controller;

import jakarta.validation.Valid;
import org.example.dtos.UserDto.PasswordChangeRequestDto;
import org.example.dtos.UserDto.UserPasswordDto;
import org.example.dtos.UserDto.UserResponseDto;
import org.example.entities.UserData;
import org.example.service.AuthService.RefreshTokenService;
import org.example.service.UserService.UserService;
import org.example.utilities.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/User")
public class UserDataController {

    UserService userService;
    RefreshTokenService refreshTokenService;
    StringRedisTemplate stringRedisTemplate;
    JwtUtil jwtUtil;

    @Autowired
    public UserDataController(UserService userService,RefreshTokenService refreshTokenService,StringRedisTemplate stringRedisTemplate,JwtUtil jwtUtil){
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/MyProfile")
    public ResponseEntity<UserResponseDto> findById(
            @RequestHeader("X-User-Id") String userIdStr
    ) {
        UserResponseDto user = userService.findById(UUID.fromString(userIdStr));
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/Logout")
    public ResponseEntity<Void> logOut(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestHeader("Authorization") String rawAuth

    ) {
        UUID userId = UUID.fromString(userIdStr);
        refreshTokenService.deleteByUserId(userId);

        String[] auth = rawAuth.split(" ");

        String accessToken = auth[1];

        long secondsLeft= jwtUtil.getRemainingSeconds(accessToken);

        if (secondsLeft > 0) {
            stringRedisTemplate.opsForValue().set(
                    accessToken,
                    "true",
                    secondsLeft,
                    TimeUnit.SECONDS
            );
        }
        return ResponseEntity.ok().build();
    }



    @PostMapping("/Delete")
    public ResponseEntity DeleteAcc(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestBody UserPasswordDto userDto
    ){
        UUID id = UUID.fromString(userIdStr);

        UserData userData = userService.findById2(id);

        String password = userDto.password();
        String realHashedPass = userData.getPassword();

        boolean passwordIsValid = BCrypt.checkpw(password, realHashedPass);

        if(!passwordIsValid){
            return ResponseEntity.badRequest().build();
        }

        boolean Executed =  userService.DeleteAccountINIT(id);

        if(Executed){
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.badRequest().build();
        }
    }


    @PutMapping("/ChangePassword")
    public ResponseEntity<UserResponseDto> ChangePassword(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestBody @Valid PasswordChangeRequestDto passwordChangeRequestDto
    ){
        String newPassword = passwordChangeRequestDto.newPassword();

        String currentPassword = passwordChangeRequestDto.oldPassword();

        UUID userUUID = UUID.fromString(userIdStr);

        return ResponseEntity.ok(userService.changePassword(userUUID, currentPassword, newPassword));
    }

    @PostMapping("/Deactivate")
    public ResponseEntity<String> deactivateAccount(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestBody UserPasswordDto userPasswordDto
            ){
        try {
            UUID id = UUID.fromString(userIdStr);
            Boolean verify = userService.passwordVerifcationViaID(id,userPasswordDto.password());


            if(verify == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Your User ID is Not Valid");
            else if(!verify) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Your Password Doesn't Verify");

            boolean isDeactivated = userService.DeactivateAccount(id);

            if (isDeactivated) {
                return ResponseEntity.ok("Account deactivated successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid UUID format.");
        }
    }


    @PostMapping("/UploadProfilePicture")
    public ResponseEntity<UserResponseDto> uploadProfilePicture(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestParam MultipartFile multipartFile
            )throws IOException {
        UserResponseDto userResponseDto =  userService.saveProfilePicture(multipartFile,UUID.fromString(userIdStr));
        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping("/IsActive")
    public ResponseEntity<Boolean> findById(@RequestParam UUID uuid){
        return ResponseEntity.ok(userService.isActive(uuid));
    }
}
