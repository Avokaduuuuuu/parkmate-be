package com.parkmate.spot.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkmate.spot.enums.SpotStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpotResponse {
    Long id;
    String name;
    Double spotTopLeftX;
    Double spotTopLeftY;
    Double spotWidth;
    Double spotHeight;
    SpotStatus status;
    String blockReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;
}
