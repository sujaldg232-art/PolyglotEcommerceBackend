package org.example.service.UserService;

import org.example.dtos.UserDto.UserRequestDto;
import org.example.dtos.UserDto.UserResponseDto;
import org.example.entities.UserData;
import org.example.mapper.UserMapper;
import org.example.repos.UserDataRepo;
import org.example.utilities.JwtUtil;
import org.example.utilities.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserDataRepo userDataRepo;
    private final UserMapper userMapper;
    private final PasswordHasher passwordHasher;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final S3Service s3service;
    private final KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    public UserService(UserDataRepo userDataRepo, UserMapper userMapper, PasswordHasher passwordHasher, JwtUtil jwtUtil, StringRedisTemplate redisTemplate,S3Service s3Service,KafkaTemplate<String,String> kafkaTemplate) {
        this.userDataRepo = userDataRepo;
        this.userMapper = userMapper;
        this.passwordHasher = passwordHasher;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.s3service = s3Service;
        this.kafkaTemplate = kafkaTemplate;
    }



    @Transactional
    public UserResponseDto userRegistration(UserRequestDto userRequestDto){

        String hashedPassword = passwordHasher.hash(userRequestDto.password());

        UserData mappedUser = userMapper.userRegtoEntity(userRequestDto);

        mappedUser.setEmail(mappedUser.getEmail().trim().toLowerCase());

        mappedUser.setPassword(hashedPassword);

        kafkaTemplate.send("userCreationTopic", mappedUser.getId().toString(),mappedUser.getId().toString());

        return userMapper.toResponseDto(userDataRepo.save(mappedUser));
    }

    @Transactional
    public UserResponseDto changePassword(
            UUID userID,
            String currentPassword,
            String newPassword
    ){
        UserData userData = userDataRepo.findById(userID).orElse(null);

        Boolean verify = passwordVerifcationViaID(userID,currentPassword);

        if(verify == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Your User ID is Not Valid");
        else if(!verify) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Your Password Doesnt Verify");


        String hashedNewPassword = passwordHasher.hash(newPassword);

        userData.setPassword(hashedNewPassword);

        userDataRepo.save(userData);

        return userMapper.toResponseDto(userData);
    }

    public UserResponseDto findById(UUID id){
        return userMapper.toResponseDto(userDataRepo.findById(id).orElse(null));
    }

    public UserData findById2(UUID id){
        return userDataRepo.findById(id).orElse(null);
    }

    public Boolean passwordVerifcationViaID(UUID uuid, String password){
        UserData userData = userDataRepo.findById(uuid).orElse(null);

        if(userData == null){
            return null;
        }

        return passwordHasher.verify(password,userData.getPassword());
    }

    public Boolean passwordVerificationViaEmail( String password,UserData userData){
        if (userData == null) {
            return false;
        }
        return passwordHasher.verify(password, userData.getPassword());
    }

    public UserData findByEmail(String email){
        return userDataRepo.findByEmail(email.toLowerCase().trim()).orElse(null);
    }

    public boolean isActive(UUID uuid){
        return userDataRepo.findById(uuid).orElse(null).isActive();
    }

    public String jwtGenerationHandler(String email, String password){
        UserData userData = userDataRepo.findByEmail(email.toLowerCase().trim()).orElse(null);
        if(userData != null && passwordVerificationViaEmail(password, userData)){
            return jwtUtil.generateToken(
                    userData.getId().toString(),
                    userData.getRole(),
                    userData.isActive(),
                    userData.isDeleted()
            );
        }
        return null;
    }

    @Transactional
    public boolean DeleteAccountINIT(UUID acc){
        UserData user = userDataRepo.findById(acc).orElse(null);
        if (user != null) {
            user.setDeleted(true);
            user.setDeletionDate(LocalDateTime.now());
            userDataRepo.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean DeactivateAccount(UUID id){
        Optional<UserData> userDataOpt = userDataRepo.findById(id);

        if (userDataOpt.isEmpty()) {
            return false;
        }

        UserData userData = userDataOpt.get();
        userData.setActive(false);
        userDataRepo.save(userData);

        return true;
    }

    public String generateAccessTokenDirectly(UUID userId) {
        UserData userData = userDataRepo.findById(userId).orElse(null);
        if (userData != null) {
            return jwtUtil.generateToken(
                    userData.getId().toString(),
                    userData.getRole(),
                    userData.isActive(),
                    userData.isDeleted()
            );
        }
        return null;
    }

    @Transactional
    public UserResponseDto saveProfilePicture(MultipartFile multipartFile, UUID uuid) throws IOException {
        String s3Key = s3service.uploadPfp(multipartFile);

        UserData user = userDataRepo.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setProfilePictureUrl(s3Key);
        userDataRepo.save(user);

        return userMapper.toResponseDto(user);
    }

    public void saveOtp(String email, String otp) {
        String normalizedEmail = email.toLowerCase().trim();
        redisTemplate.opsForValue().set("OTP:" + normalizedEmail, otp, Duration.ofMinutes(15));
    }

    @Transactional
    public String verifyOtp(String email, String givenOtp){
        String normalizedEmail = email.toLowerCase().trim();
        String cachedOtp = redisTemplate.opsForValue().get("OTP:" + normalizedEmail);

        if (cachedOtp != null && cachedOtp.equals(givenOtp)) {
            redisTemplate.delete("OTP:" + normalizedEmail);

            String resetToken = UUID.randomUUID().toString();

            redisTemplate.opsForValue().set("OTPVAL:" + normalizedEmail, resetToken, Duration.ofMinutes(15));
            return resetToken;
        }
        return null;
    }


    @Transactional
    public boolean resetPassword(String email, String resetToken, String newPassword) {
        if (resetToken == null || resetToken.isEmpty()) {
            return false;
        }

        String normalizedEmail = email.toLowerCase().trim();
        String cachedToken = redisTemplate.opsForValue().get("OTPVAL:" + normalizedEmail);

        if (cachedToken != null && cachedToken.equals(resetToken)) {
            String hashedPassword = passwordHasher.hash(newPassword);
            int rowsUpdated = userDataRepo.updatePassword(normalizedEmail, hashedPassword);

            if (rowsUpdated > 0) {
                redisTemplate.delete("OTPVAL:" + normalizedEmail);
                return true;
            }
        }

        return false;
    }
}