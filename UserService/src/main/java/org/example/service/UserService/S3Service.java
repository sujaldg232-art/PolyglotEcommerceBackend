package org.example.service.UserService;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.pfpbucket}")
    private String profileBucket;

    @Value("${cloud.aws.cred.access-key}")
    private String accessKey;

    @Value("${cloud.aws.cred.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Autowired
    public S3Service(S3Client s3Client){
        this.s3Client = s3Client;
    }

    public String uploadPfp(MultipartFile file) throws IOException {
        String type = checkFileType(file);
        String randomString = UUID.randomUUID().toString();
        String key = randomString + type;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(profileBucket)
                        .key(key)
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        return key;
    }

    public String checkFileType(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }
        try (InputStream is = file.getInputStream()) {
            Tika tika = new Tika();
            String detectedType = tika.detect(is);

            return switch (detectedType) {
                case "image/jpeg" -> ".jpg";
                case "image/png" -> ".png";
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Security Alert: Invalid file format detected.");
            };
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading file stream", e);
        }
    }

    public String createPresignedGetUrl(String bucketName, String keyName) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build()) {

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

            return presignedRequest.url().toExternalForm();
        }
    }
}