package com.parkmate.momo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MoMoIPNResponse {

    @JsonProperty("partnerCode")
    private String partnerCode;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("resultCode")
    private Integer resultCode;  // 0 = Backend đã xử lý thành công

    @JsonProperty("message")
    private String message;  // VD: "Success" hoặc "Error processing payment"

    @JsonProperty("responseTime")
    private Long responseTime;  // Timestamp hiện tại

    /**
     * Factory method: Tạo success response
     */
    public static MoMoIPNResponse success(String partnerCode, String requestId, String orderId) {
        return MoMoIPNResponse.builder()
                .partnerCode(partnerCode)
                .requestId(requestId)
                .orderId(orderId)
                .resultCode(0)
                .message("Success")
                .responseTime(System.currentTimeMillis())
                .build();
    }

    /**
     * Factory method: Tạo error response
     */
    public static MoMoIPNResponse error(String partnerCode, String requestId, String orderId, String errorMessage) {
        return MoMoIPNResponse.builder()
                .partnerCode(partnerCode)
                .requestId(requestId)
                .orderId(orderId)
                .resultCode(1)  // Non-zero = error
                .message(errorMessage)
                .responseTime(System.currentTimeMillis())
                .build();
    }

}
