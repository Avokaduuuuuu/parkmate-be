package com.parkmate.s3;

import com.parkmate.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parking-service/s3")
@RequiredArgsConstructor
@Tag(name = "S3 File Management API", description = "API for uploading and managing parking lot images in AWS S3")
public class S3Controller {

    private final S3Service s3Service;

    @PostMapping(value = "/{id}/lots", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload parking lot images",
            description = """
                    Upload multiple images for a specific parking lot to AWS S3 storage.
                    Images will be stored securely and accessible via CDN URLs.
                    
                    **Path Parameters:**
                    - `id` (required): The unique identifier of the parking lot
                    
                    **Request Body:**
                    - `images` (required): List of image files to upload (multipart/form-data)
                    
                    **Supported Image Formats:**
                    - JPEG/JPG (.jpg, .jpeg)
                    - PNG (.png)
                    - WebP (.webp)
                    - GIF (.gif)
                    
                    **File Requirements:**
                    - Maximum file size: 10 MB per image
                    - Maximum number of images: 20 per request
                    - Images should be clear and well-lit
                    - Recommended resolution: 1920x1080 or higher
                    
                    **Image Types Recommended:**
                    - Exterior views of the parking lot entrance
                    - Interior views showing parking spaces
                    - Navigation signs and wayfinding
                    - Facility amenities (elevators, payment kiosks)
                    - Parking spot examples
                    - Security features
                    
                    **Upload Process:**
                    1. Images are validated for format and size
                    2. Files are uploaded to AWS S3 with unique identifiers
                    3. Images are associated with the specified parking lot
                    4. CDN URLs are generated for fast access
                    5. Thumbnails may be automatically generated
                    
                    **Returns:** List of uploaded image details including:
                    - Image ID and filename
                    - S3 storage key/path
                    - Public access URL
                    - Upload timestamp
                    - Image metadata (size, format, dimensions)
                    
                    **Use Cases:**
                    - Adding photos during parking lot registration
                    - Updating parking lot gallery
                    - Providing visual reference for customers
                    - Enhancing parking lot listings
                    - Marketing and promotional materials
                    
                    **Important Notes:**
                    - Parking lot must exist before uploading images
                    - Previous images are not deleted automatically
                    - Ensure images do not contain sensitive personal information
                    - Images should comply with privacy regulations
                    """
    )
    public ResponseEntity<?> uploadParkingLotImages(
            @Parameter(
                    description = "Unique identifier of the parking lot to upload images for",
                    required = true,
                    example = "1"
            )
            @PathVariable("id") Long lotId,

            @Parameter(
                    description = "List of image files to upload. Supported formats: JPG, PNG, WebP, GIF. Max size: 10MB per file.",
                    required = true
            )
            @RequestParam List<MultipartFile> images
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Upload images successfully for lots " + lotId,
                                s3Service.uploadParkingImages(lotId, images)
                        )
                );
    }

    @DeleteMapping
    @Operation(
            summary = "Delete parking lot images",
            description = """
                Delete multiple images from AWS S3 storage and remove their records from the database.
                This is a permanent operation and cannot be undone.
                
                **Request Body:**
                - `imageIds` (required): List of image IDs to delete
                
                **Behavior:**
                1. Validates that all image IDs exist
                2. Removes images from AWS S3 storage
                3. Deletes image records from the database
                4. Updates parking lot image associations
                5. Returns confirmation of deletion
                
                **Business Rules:**
                - Only the parking lot owner or admin can delete images
                - Cannot delete images if they are being used in active promotions
                - At least one image should remain for the parking lot (optional rule)
                - Deletion is permanent and cannot be reversed
                
                **Use Cases:**
                - Removing outdated or incorrect images
                - Cleaning up duplicate images
                - Replacing poor quality photos
                - Removing images that violate policies
                - Managing parking lot gallery content
                
                **Important Notes:**
                - Images are permanently deleted from S3 storage
                - Thumbnail versions are also deleted
                - Any cached CDN versions will eventually expire
                - Image URLs will become invalid immediately
                - Consider backing up important images before deletion
                
                **Error Scenarios:**
                - 400: Invalid image IDs or empty list
                - 403: Insufficient permissions to delete images
                - 404: One or more image IDs not found
                - 500: S3 deletion failure
                
                **Security:**
                - Requires authentication
                - Validates ownership of images
                - Logs deletion actions for audit trail
                """
    )
    public ResponseEntity<?> deleteImages(
            @Parameter(
                    description = "List of image IDs to delete. All IDs must be valid and exist in the system.",
                    required = true,
                    example = "[1, 2, 3, 4, 5]",
                    schema = @Schema(type = "array", example = "[1, 2, 3]")
            )
            @RequestBody
            @NotEmpty(message = "Image IDs list cannot be empty")
            @Size(min = 1, max = 50, message = "Can delete between 1 and 50 images at a time")
            List<@Positive(message = "Image ID must be positive") Long> imageIds
    ) {
        s3Service.deleteImages(imageIds);
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.success("Images deleted successfully")
                );
    }
}