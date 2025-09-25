package com.parkmate.mobileDevice.dto;

import com.parkmate.mobileDevice.DeviceOs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "MobileDeviceSearchCriteria",
        description = "Search criteria for filtering mobile devices with various filter options"
)
public class MobileDeviceSearchCriteria {

    // Single values
    @Schema(
            description = "Filter by specific user ID",
            example = "123",
            nullable = true
    )
    private Long userId;

    @Schema(
            description = "Filter by device operating system",
            example = "ANDROID",
            allowableValues = {"ANDROID", "IOS"},
            nullable = true
    )
    private DeviceOs deviceOs;

    // Multiple values
    @Schema(
            description = "Filter by multiple user IDs",
            example = "[123, 456, 789]",
            nullable = true
    )
    private List<Long> userIds;

    @Schema(
            description = "Filter by multiple device operating systems",
            example = "[\"ANDROID\", \"IOS\"]",
            allowableValues = {"ANDROID", "IOS"},
            nullable = true
    )
    private List<DeviceOs> deviceOsList;

    // Other fields
    @Schema(
            description = "Filter by exact device ID match",
            example = "perf_device_000123",
            nullable = true
    )
    private String deviceId;

    @Schema(
            description = "Filter by device name (partial match supported)",
            example = "iPhone 15",
            nullable = true
    )
    private String deviceName;

    @Schema(
            description = "Filter by device active status",
            example = "true",
            nullable = true
    )
    private Boolean isActive;

    @Schema(
            description = "Filter devices that were active after this date",
            example = "2024-01-01T00:00:00",
            type = "string",
            format = "date-time",
            nullable = true
    )
    private LocalDateTime lastActiveAfter;

    @Schema(
            description = "Filter devices that were active before this date",
            example = "2024-12-31T23:59:59",
            type = "string",
            format = "date-time",
            nullable = true
    )
    private LocalDateTime lastActiveBefore;

    @Schema(
            description = "Filter devices created after this date",
            example = "2024-01-01T00:00:00",
            type = "string",
            format = "date-time",
            nullable = true
    )
    private LocalDateTime createdAfter;

    @Schema(
            description = "Filter devices created before this date",
            example = "2024-12-31T23:59:59",
            type = "string",
            format = "date-time",
            nullable = true
    )
    private LocalDateTime createdBefore;

    @Schema(
            description = "Filter devices inactive for more than specified days",
            example = "30",
            minimum = "0",
            maximum = "365",
            nullable = true
    )
    private Integer inactiveDays;
}