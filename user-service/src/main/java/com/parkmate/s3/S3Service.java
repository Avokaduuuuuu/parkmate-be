package com.parkmate.s3;

import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public String uploadFile(MultipartFile file, String folderPath, String filePrefix) throws IOException {
        log.info("Starting file upload - FileName: {}, ContentType: {}, Size: {}",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        if (file.isEmpty()) {
            log.error("File is empty");
            throw new AppException(ErrorCode.FILE_EMPTY);
        }
        if (!isValidFileSize(file)) {
            log.error("File size exceeded: {} bytes", file.getSize());
            throw new AppException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        if (!isValidImageFile(file)) {
            log.error("Invalid file type: {}", file.getContentType());
            throw new AppException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = String.format("%s/%s-%s%s",
                folderPath,
                filePrefix,
                UUID.randomUUID(),
                extension);

        try {
            log.info("Uploading to S3 - Bucket: {}, Key: {}, Region: {}", bucketName, fileName, region);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            // Return S3 key instead of public URL
            log.info("File uploaded successfully - Key: {}", fileName);
            return fileName;
        } catch (S3Exception e) {
            log.error("S3 upload failed - Error: {}, Message: {}", e.awsErrorDetails().errorCode(), e.getMessage(), e);
            throw new AppException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract key from URL
            String key = extractKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (S3Exception e) {
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage(), e);
        }
    }

    public boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/jpg") ||
                        contentType.equals("image/webp"));
    }

    public boolean isValidFileSize(MultipartFile file) {
        log.info("Validating file size: {} bytes", file.getSize());
        long maxSize = 10 * 1024 * 1024;
        return file.getSize() <= maxSize;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String buildPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                key
        );
    }

    private String extractKeyFromUrl(String url) {
        // URL format: https://parkmate-storage.s3.ap-southeast-1.amazonaws.com/users/user-1/profile/avatar-uuid.jpg
        // Extract: users/user-1/profile/avatar-uuid.jpg
        String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
        return url.replace(baseUrl, "");
    }

    public String generatePresignedUrl(String s3Key) {
        if (s3Key == null || s3Key.isEmpty()) {
            return null;
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1)) // URL valid for 1 hour
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (S3Exception e) {
            log.error("Failed to generate presigned URL for key: {}", s3Key, e);
            return null;
        }
    }


}
