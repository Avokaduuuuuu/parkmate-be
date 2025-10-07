package com.parkmate.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class MomoConfig {

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.ipn-url}")
    private String ipnUrl;

    @Value("${momo.redirect-url}")
    private String redirectUrl;

    @Value("${momo.request-type}")
    private String requestType;

    @Value("${momo.lang}")
    private String lang;

    @Value("${momo.timeout}")
    private int timeout;


}
