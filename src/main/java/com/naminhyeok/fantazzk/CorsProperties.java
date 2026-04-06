package com.naminhyeok.fantazzk;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        if (allowedOrigins == null) {
            allowedOrigins = List.of("http://localhost:3000", "http://localhost:5173");
        }
    }

    public CorsProperties() {
        this(List.of("http://localhost:3000", "http://localhost:5173"));
    }
}
