package com.parkmate.s3;

import com.parkmate.image.ImageEntity;
import com.parkmate.image.dto.resp.ImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Service {
    List<ImageResponse> uploadParkingImages(Long parkingLotId,List<MultipartFile> files);
    List<ImageEntity> uploadParkingImagesWhenCreate(Long parkingLotId, List<MultipartFile> files);
    String getPresignedUrl(String key);

    void deleteImages(List<Long> imageIds);
}
