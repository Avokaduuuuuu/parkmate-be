package com.parkmate.vehicle.dto;

import com.parkmate.vehicle.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddedVehicleInfo {
    private Long vehicleId;
    private Long userId;
    private String licensePlate;
    private VehicleType vehicleType;
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleColor;
    private boolean isElectric;
    private LocalDateTime addedAt;

    /**
     * DTO for sync API response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncResponse {
        private Long userId;
        private String licensePlate;
        private VehicleType vehicleType;
    }

    public SyncResponse toSyncResponse() {
        return SyncResponse.builder()
                .userId(this.userId)
                .licensePlate(this.licensePlate)
                .vehicleType(this.vehicleType)
                .build();
    }
}
