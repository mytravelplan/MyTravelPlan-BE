package travel.mytravelplan.infra.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import travel.mytravelplan.global.error.code.AmazonS3ErrorCode;
import travel.mytravelplan.infra.s3.exception.AwsException;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AmazonS3Client {
    private final S3Presigner s3Presigner;

    private final S3Client s3Client;

    public String generatePresignedUrl(String bucketName, String keyName, String contentType, Long contentLength, Instant expiration) {
        try {
            Duration duration = Duration.between(Instant.now(), expiration);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (S3Exception e) {
            throw new AwsException(AmazonS3ErrorCode.FAILED_TO_GENERATE_PRESIGNED_URL);
        }

    }

    public void deleteObject(String bucketName, String keyName) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .build();

            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            throw new AwsException(AmazonS3ErrorCode.FAILED_TO_DELETE_OBJECT);
        }
    }

/*
    public String getPresignedUrl(String bucketName, String keyName, Instant expiration) {
        Duration duration = Duration.between(Instant.now(), expiration);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(request);
        return presigned.toString();
    }
*/
}