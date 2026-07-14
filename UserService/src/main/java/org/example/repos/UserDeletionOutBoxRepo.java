package org.example.repos;

import org.example.entities.OutBoxStatusEnum;
import org.example.entities.UserDeletionOutBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface UserDeletionOutBoxRepo extends JpaRepository<UserDeletionOutBox, UUID> {


    List<UserDeletionOutBox> findAllByStatus(OutBoxStatusEnum outBoxStatusEnum);

}
