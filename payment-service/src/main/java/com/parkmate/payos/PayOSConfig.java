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
    // Payment configuration (for top-up)
    private String clientId;
    private String apiKey;
    private String checksumKey;

    // Payout configuration (for withdrawals)
    private String payoutClientId;      // Payout Client ID (separate from payment)
    private String payoutApiKey;        // Payout API Key
    private String payoutChecksumKey;   // Payout Checksum Key (separate from payment)

    // URLs
    private String returnUrl;
    private String cancelUrl;
}