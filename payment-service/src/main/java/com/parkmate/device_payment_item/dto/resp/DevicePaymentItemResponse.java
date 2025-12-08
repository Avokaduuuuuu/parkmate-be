package com.parkmate.device_payment_item.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.common.enums.DeviceType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DevicePaymentItemResponse {
    Long id;
    DeviceType deviceType;
    Integer totalDevice;
    BigDecimal deviceFee;
    BigDecimal totalFee;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;

}
