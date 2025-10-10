package com.parkmate.s3;

import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import com.parkmate.partner.PartnerRepository;
import com.parkmate.partnerRegistration.PartnerRegistration;
import com.parkmate.partnerRegistration.PartnerRegistrationRepository;
import com.parkmate.user.User;
import com.parkmate.user.UserRepository;
import com.parkmate.vehicle.Vehicle;
import com.parkmate.vehicle.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final PartnerRepository partnerRepository;
    private final PartnerRegistrationRepository partnerRegistrationRepository;


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

    // Tải ảnh lên bằng cách copy s3Key bằng tay vào request body
    public String uploadImage(MultipartFile file, ImageType imageType) throws IOException {
        String folderPath = determineFolderPath(imageType);
        String filePrefix = determineFilePrefix(imageType);
        return uploadFile(file, folderPath, filePrefix);
    }

    // Tải ảnh lên và tự động cập nhật entity
    // Phải tạo entity trước và đưa id xuống
    @Transactional
    public String uploadImageAndUpdate(MultipartFile file, Long entityId, ImageType imageType) throws IOException {
        String folderPath = determineFolderPathWithEntity(entityId, imageType);
        String filePrefix = determineFilePrefix(imageType);
        String s3Key = uploadFile(file, folderPath, filePrefix);

        // Auto-update entity with s3Key
        updateEntityImage(entityId, imageType, s3Key);

        return s3Key;
    }

    private String determineFolderPath(ImageType imageType) {
        return switch (imageType) {
            case AVATAR -> "users/avatars";
            case VEHICLE_IMAGE -> "vehicles/vehicle-images";
            case PARTNER_BUSINESS_LICENSE -> "partners/business-licenses";
            case FRONT_ID_CARD, BACK_ID_CARD -> "partners/id-cards";
        };
    }

    private String determineFolderPathWithEntity(Long entityId, ImageType imageType) {
        return switch (imageType) {
            case AVATAR -> String.format("member/member-%d/profile", entityId);
            case VEHICLE_IMAGE -> String.format("vehicles/vehicle-%d", entityId);
            case PARTNER_BUSINESS_LICENSE -> String.format("partners/partner-%d/business", entityId);
            case FRONT_ID_CARD, BACK_ID_CARD -> String.format("member/member-%d/id-cards", entityId);
        };
    }

    private String determineFilePrefix(ImageType imageType) {
        return switch (imageType) {
            case AVATAR -> "avatar";
            case VEHICLE_IMAGE -> "vehicle-image";
            case PARTNER_BUSINESS_LICENSE -> "business-license";
            case FRONT_ID_CARD -> "front-id";
            case BACK_ID_CARD -> "back-id";
        };
    }

    @Transactional
    protected void updateEntityImage(Long entityId, ImageType imageType, String s3Key) {
        log.info("Updating entity image - EntityId: {}, ImageType: {}, S3Key: {}", entityId, imageType, s3Key);

        switch (imageType) {
            case AVATAR -> {
                User user = userRepository.findById(entityId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                user.setProfilePictureUrl(s3Key);
                userRepository.save(user);
                log.info("Updated user {} avatar to {}", entityId, s3Key);
            }
            case VEHICLE_IMAGE -> {
                Vehicle vehicle = vehicleRepository.findById(entityId)
                        .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
                vehicle.setLicenseImage(s3Key);
                vehicleRepository.save(vehicle);
                log.info("Updated vehicle {} license plate image to {}", entityId, s3Key);
            }
            case PARTNER_BUSINESS_LICENSE -> {
                PartnerRegistration partnerRegistration = partnerRegistrationRepository.findById(entityId)
                        .orElseThrow(() -> new AppException(ErrorCode.PARTNER_NOT_FOUND));
                partnerRegistration.setBusinessLicenseFileUrl(s3Key);
                partnerRegistrationRepository.save(partnerRegistration);
                log.info("Updated partner registration {} business license to {}", entityId, s3Key);
            }
            case FRONT_ID_CARD -> {
                User user = userRepository.findById(entityId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                user.setFrontPhotoPath(s3Key);
                userRepository.save(user);
                log.info("Updated user {} front ID card to {}", entityId, s3Key);
            }
            case BACK_ID_CARD -> {
                User user = userRepository.findById(entityId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                user.setBackPhotoPath(s3Key);
                userRepository.save(user);
                log.info("Updated user {} back ID card to {}", entityId, s3Key);
            }
        }
    }

}
