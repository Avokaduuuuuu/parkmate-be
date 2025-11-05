package com.parkmate.client.config;

import com.parkmate.client.exception.FeignErrorDecoder;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign client configuration.
 * Registers custom ErrorDecoder to handle errors from other microservices.
 */
@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    private final FeignErrorDecoder feignErrorDecoder;

    @Bean
    public ErrorDecoder errorDecoder() {
        return feignErrorDecoder;
    }
}