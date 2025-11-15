package com.parkmate.payos;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
@RequiredArgsConstructor
public class PayOSBeanConfig {

    private final PayOSConfig payOSConfig;

    @Bean(name = "payOSPayment")
    public PayOS payOSPayment() {
        // For Payment (top-up) operations
        return new PayOS(
                payOSConfig.getClientId(),
                payOSConfig.getApiKey(),
                payOSConfig.getChecksumKey()
        );
    }

    @Bean(name = "payOSPayout")
    public PayOS payOSPayout() {
        // For Payout (withdrawal) operations
        String payoutApiKey = payOSConfig.getPayoutApiKey();

        // Fallback to payment API key if payout key not configured
        if (payoutApiKey == null || payoutApiKey.isEmpty() || "YOUR_PAYOUT_API_KEY_HERE".equals(payoutApiKey)) {
            payoutApiKey = payOSConfig.getApiKey();
        }

        return new PayOS(
                payOSConfig.getClientId(),
                payoutApiKey,
                payOSConfig.getChecksumKey()
        );
    }

    @Bean
    public PayOS payOS() {
        // Default bean for backward compatibility
        return payOSPayment();
    }
}