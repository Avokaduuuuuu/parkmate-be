package com.parkmate.payos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payos")
@Getter
@Setter
public class PayOSConfig {
    private String clientId;
    private String apiKey;           // Payment API Key
    private String payoutApiKey;     // Payout API Key (for withdrawals)
    private String checksumKey;
    private String returnUrl;
    private String cancelUrl;
}