package org.example.service.AuthService;

import jakarta.persistence.EntityNotFoundException;
import org.example.entities.RefreshToken;
import org.example.entities.UserData;
import org.example.repos.RefreshTokenRepository;
import org.example.repos.UserDataRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDataRepo userDataRepo;

    @Value("${app.jwt.refresh-expiration-ms:2592000000}")
    private long refreshExpirationMs;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserDataRepo userDataRepo) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userDataRepo = userDataRepo;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }


    public void deleteByUserDat(UserData userData){
        refreshTokenRepository.deleteByUserDat(userData);
    }


    @Transactional
    public RefreshToken createRefreshToken(UUID userId) {
        UserData user = userDataRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        refreshTokenRepository.deleteByUserDat(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserDat(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            return null;
        }
        return token;
    }
    @Transactional
    public void deleteByUserId(UUID userId) {
        UserData user = userDataRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        refreshTokenRepository.deleteByUserDat(user);
    }
}