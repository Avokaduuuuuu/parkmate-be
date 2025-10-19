package com.parkmate.payos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PayOSWebhookRequest {
    @JsonProperty("orderCode")
    private Long orderCode;
    
    @JsonProperty("amount")
    private Integer amount;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("accountNumber")
    private String accountNumber;
    
    @JsonProperty("reference")
    private String reference;
    
    @JsonProperty("transactionDateTime")
    private String transactionDateTime;
    
    @JsonProperty("code")
    private String code;  // "00" = success
    
    @JsonProperty("desc")
    private String desc;
    
    @JsonProperty("counterAccountBankId")
    private String counterAccountBankId;
    
    @JsonProperty("counterAccountName")
    private String counterAccountName;
    
    @JsonProperty("counterAccountNumber")
    private String counterAccountNumber;
    
    @JsonProperty("virtualAccountName")
    private String virtualAccountName;
    
    @JsonProperty("virtualAccountNumber")
    private String virtualAccountNumber;
    
    public boolean isSuccess() {
        return "00".equals(code);
    }
}