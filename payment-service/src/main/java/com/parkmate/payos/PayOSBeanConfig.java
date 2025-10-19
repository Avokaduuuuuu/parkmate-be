package com.parkmate.payos;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
@RequiredArgsConstructor
public class PayOSBeanConfig {
    
    private final PayOSConfig payOSConfig;
    
    @Bean
    public PayOS payOS() {
        return new PayOS(
            payOSConfig.getClientId(),
            payOSConfig.getApiKey(),
            payOSConfig.getChecksumKey()
        );
    }
}