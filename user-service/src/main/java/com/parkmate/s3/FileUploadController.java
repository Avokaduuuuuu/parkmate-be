package com.parkmate.s3;

import com.parkmate.common.dto.ApiResponse;
import com.parkmate.s3.dto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/user-service/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final S3Service s3Service;

    /**
     * Upload image without entityId (pre-upload scenario)
     * Returns s3Key to be included in create request
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponse> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam("imageType") ImageType imageType
    ) throws IOException {
        log.info("Uploading image - ImageType: {}", imageType);

        String s3Key = s3Service.uploadImage(file, imageType);
        String presignedUrl = s3Service.generatePresignedUrl(s3Key);

        ImageUploadResponse response = ImageUploadResponse.builder()
                .s3Key(s3Key)
                .presignedUrl(presignedUrl)
                .build();

        return ApiResponse.success(response, "Image uploaded successfully");
    }

    /**
     * Upload image with entityId and auto-update entity
     * Entity must exist before uploading
     */
    @PostMapping(value = "/image/entity", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponse> uploadImageWithEntity(
            @RequestPart("file") MultipartFile file,
            @RequestParam("entityId") Long entityId,
            @RequestParam("imageType") ImageType imageType
    ) throws IOException {
        log.info("Uploading image with entity - EntityId: {}, ImageType: {}", entityId, imageType);

        String s3Key = s3Service.uploadImageAndUpdate(file, entityId, imageType);
        String presignedUrl = s3Service.generatePresignedUrl(s3Key);

        ImageUploadResponse response = ImageUploadResponse.builder()
                .s3Key(s3Key)
                .presignedUrl(presignedUrl)
                .build();

        return ApiResponse.success(response, "Image uploaded and entity updated successfully");
    }
}
