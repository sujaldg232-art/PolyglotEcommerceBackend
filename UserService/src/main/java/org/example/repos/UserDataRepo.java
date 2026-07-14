package org.example.repos;


import org.example.entities.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDataRepo extends JpaRepository<UserData, UUID> {
    Optional<UserData> findByEmail(String email);

    @Modifying
    @Query(value = "UPDATE user_data SET password = :password WHERE email = :email", nativeQuery = true)
    int updatePassword(@Param("email") String email, @Param("password") String password);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_data WHERE user_data.is_deleted = true AND user_data.deletion_date < :time", nativeQuery = true)
    void deleteByIsDeletedTrueAndDeletionDateBefore(@Param("time") LocalDateTime time);

    @Query(value = "SELECT u FROM UserData u LEFT JOIN FETCH u.addresses WHERE u.isDeleted = true AND u.deletionDate < :time")
    List<UserData> findAllUserByIsDeletedTrueAndDeletionDateBefore(@Param("time") LocalDateTime time);



 }
