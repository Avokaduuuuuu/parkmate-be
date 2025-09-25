package com.parkmate.dto.request;

import com.parkmate.dto.criteria.MobileDeviceSearchCriteria;
import com.parkmate.entity.enums.DeviceOs;
import com.parkmate.util.ParseUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MobileDeviceSearchRequest {
    // Smart fields
    private String userIds;
    private String deviceOsList;


    // Single fields
    private String deviceId;
    private Boolean isActive;
    private String deviceName;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveAfter;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActiveBefore;

    private Integer inactiveDays;

    public MobileDeviceSearchCriteria toCriteria() {
        MobileDeviceSearchCriteria.MobileDeviceSearchCriteriaBuilder builder = MobileDeviceSearchCriteria.builder();

        if (StringUtils.hasText(this.userIds)) {
            List<Long> userIdList = ParseUtil.parseLongList(this.userIds, "userIds");
            if (userIdList.size() == 1) {
                builder.userId(userIdList.get(0));
            } else {
                builder.userIds(userIdList);
            }
        }

        if (StringUtils.hasText(this.deviceOsList)) {
            List<DeviceOs> osList = ParseUtil.parseEnumList(this.deviceOsList, DeviceOs.class, "deviceOsList");
            if (osList.size() == 1) {
                builder.deviceOs(osList.get(0));
            } else {
                builder.deviceOsList(osList);
            }
        }

        builder.deviceId(this.deviceId)
                .isActive(this.isActive)
                .deviceName(this.deviceName)
                .lastActiveAfter(this.lastActiveAfter)
                .lastActiveBefore(this.lastActiveBefore);
        return builder.build();
    }

    // Helper methods same as VehicleSearchRequest
}