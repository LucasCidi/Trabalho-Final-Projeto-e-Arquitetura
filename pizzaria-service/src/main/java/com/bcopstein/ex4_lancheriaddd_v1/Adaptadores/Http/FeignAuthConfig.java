package com.bcopstein.ex4_lancheriaddd_v1.Adaptadores.Http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor jwtAuthInterceptor(@Value("${app.internal.jwt-token}") String jwtToken) {

        return template -> template.header("Authorization", "Bearer " + jwtToken);
    }
}