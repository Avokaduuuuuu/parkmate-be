package com.parkmate.userSubscription.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.userSubscription.UserSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSubscriptionSyncResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private Long vehicleId;
    private Long vehicleType;
    private String vehicleLicensePlate;
    private Long subscriptionPackageId;
    private Long parkingLotId;
    private Long assignedSpotId;
    private String assignedSpotName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    private UserSubscriptionStatus status;


}
