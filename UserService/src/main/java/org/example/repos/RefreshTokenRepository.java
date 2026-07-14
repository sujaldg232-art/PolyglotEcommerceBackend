package org.example.repos;

import org.example.entities.RefreshToken;
import org.example.entities.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);



    @Transactional
    void deleteByUserDatIdIn(List<UUID> userIds);

    @Transactional
    void deleteByUserDat(UserData user);

    @Transactional
    void deleteByUserDatIn(List<UserData> users);


    Optional<RefreshToken> findByUserDat(UserData userData);

}