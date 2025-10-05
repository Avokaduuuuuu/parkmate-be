package com.parkmate.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {
    private String s3Key;
    private String presignedUrl;
}