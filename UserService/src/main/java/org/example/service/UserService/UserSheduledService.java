
package org.example.service.UserService;

import org.example.entities.OutBoxStatusEnum;
import org.example.entities.Role;
import org.example.entities.UserData;
import org.example.entities.UserDeletionOutBox;
import org.example.repos.RefreshTokenRepository;
import org.example.repos.UserDataRepo;
import org.example.repos.UserDeletionOutBoxRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserSheduledService {
    private final UserDataRepo userDataRepo;
    private final UserDeletionOutBoxRepo userDeletionOutBoxRepo;
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public UserSheduledService(UserDataRepo userDataRepo, UserDeletionOutBoxRepo userDeletionOutBoxRepo,KafkaTemplate<String,String> kafkaTemplate,RefreshTokenRepository refreshTokenRepository){
        this.userDataRepo = userDataRepo;
        this.userDeletionOutBoxRepo = userDeletionOutBoxRepo;
        this.kafkaTemplate = kafkaTemplate;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0/10 * * * * ?")
    @Transactional
    public void purgeDeletedUsers() {
        LocalDateTime now = LocalDateTime.now();

        List<UserData> listOfUsers = userDataRepo.findAllUserByIsDeletedTrueAndDeletionDateBefore(now);

        List<UserDeletionOutBox> outboxRecords = listOfUsers.stream()
                .filter(user -> user.getRole() != Role.BUYER)
                .map(user -> UserDeletionOutBox.builder()
                        .userUuid(user.getId())
                        .status(OutBoxStatusEnum.INPROCESS)
                        .build())
                .collect(Collectors.toList());

        if (!outboxRecords.isEmpty()) {
            userDeletionOutBoxRepo.saveAll(outboxRecords);
        }

        if (!listOfUsers.isEmpty()) {
            refreshTokenRepository.deleteByUserDatIn(listOfUsers);
            userDataRepo.deleteAll(listOfUsers);
        }
    }



    @Scheduled(cron = "0/30 * * * * ?")
    public void userKafkaDeletion(){
        List<UserDeletionOutBox> userDeletionOutBoxes = userDeletionOutBoxRepo.findAllByStatus(OutBoxStatusEnum.INPROCESS);

        for (UserDeletionOutBox outbox : userDeletionOutBoxes) {
            try {
                kafkaTemplate.send("userDeletionTopic", outbox.getUserUuid().toString(), outbox.getUserUuid().toString()).get();
                userDeletionOutBoxRepo.delete(outbox);
            } catch (Exception e) {
                outbox.setStatus(OutBoxStatusEnum.INPROCESS);
                userDeletionOutBoxRepo.save(outbox);
            }
        }
    }
}