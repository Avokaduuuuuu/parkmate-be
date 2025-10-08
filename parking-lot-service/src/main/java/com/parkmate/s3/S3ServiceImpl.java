package com.parkmate.s3;

import com.parkmate.exception.AppException;
import com.parkmate.exception.ErrorCode;
import com.parkmate.image.ImageEntity;
import com.parkmate.image.ImageMapper;
import com.parkmate.image.ImageRepository;
import com.parkmate.image.dto.resp.ImageResponse;
import com.parkmate.parking_lot.ParkingLotEntity;
import com.parkmate.parking_lot.ParkingLotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service{
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ParkingLotRepository parkingLotRepository;
    private final ImageRepository imageRepository;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public List<ImageResponse> uploadParkingImages(Long parkingLotId, List<MultipartFile> files) {
        ParkingLotEntity parkingLotEntity = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND, "Parking Lot with id " + parkingLotId + " not found"));

        List<ImageEntity> imageEntities = new ArrayList<>();
        String folderName = "lots";
        String prefix = parkingLotEntity.getId() + "_" + parkingLotEntity.getName();
        for (MultipartFile file : files) {
            String fileName = folderName + "/" +  prefix + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            try {
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .key(fileName)
                                .contentType(file.getContentType())
                                .bucket(bucketName)
                                .build(),
                        RequestBody.fromBytes(file.getBytes())
                );
                ImageEntity imageEntity = new ImageEntity();
                imageEntity.setPath(fileName);
                imageEntity.setParkingLot(parkingLotEntity);
                imageEntity.setIsActive(true);
                imageEntities.add(imageEntity);
            } catch (IOException e) {
                throw new AppException(ErrorCode.INVALID_IMAGE, e.getMessage());
            }
        }
        parkingLotEntity.getImages().addAll(imageEntities);
        parkingLotRepository.save(parkingLotEntity);
        return imageEntities.stream().map(ImageMapper.INSTANCE::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ImageEntity> uploadParkingImagesWhenCreate(Long parkingLotId, List<MultipartFile> files) {
        ParkingLotEntity parkingLotEntity = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new AppException(ErrorCode.PARKING_NOT_FOUND, "Parking Lot with id " + parkingLotId + " not found"));

        List<ImageEntity> imageEntities = new ArrayList<>();
        String folderName = "lots";
        String prefix = parkingLotEntity.getId() + "_" + parkingLotEntity.getName();
        for (MultipartFile file : files) {
            String fileName = folderName + "/" +  prefix + "/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            try {
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .key(fileName)
                                .contentType(file.getContentType())
                                .bucket(bucketName)
                                .build(),
                        RequestBody.fromBytes(file.getBytes())
                );
                ImageEntity imageEntity = new ImageEntity();
                imageEntity.setPath(fileName);
                imageEntity.setParkingLot(parkingLotEntity);
                imageEntity.setIsActive(true);
                imageEntities.add(imageEntity);
            } catch (IOException e) {
                throw new AppException(ErrorCode.INVALID_IMAGE, e.getMessage());
            }
        }
        parkingLotEntity.getImages().addAll(imageEntities);
        parkingLotRepository.save(parkingLotEntity);
        return imageEntities;
    }

    @Override
    public String getPresignedUrl(String key) {
        String url = "";
        if (key != null && !key.isEmpty()) {
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(24))
                    .getObjectRequest(GetObjectRequest.builder().bucket(bucketName).key(key).build())
                    .build();
            url = s3Presigner.presignGetObject(getObjectPresignRequest).url().toString();
        }
        return url;
    }

    @Override
    public void deleteImages(List<Long> imageIds) {
        List<ImageEntity> imageEntities = imageRepository.findAllById(imageIds);
        for (ImageEntity imageEntity : imageEntities) {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(imageEntity.getPath())
                            .build()
            );
        }
        imageRepository.deleteAll(imageEntities);
    }
}
