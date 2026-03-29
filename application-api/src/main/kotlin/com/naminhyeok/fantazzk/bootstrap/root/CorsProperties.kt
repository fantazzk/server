package com.naminhyeok.fantazzk.bootstrap.root

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cors")
data class CorsProperties(
    val allowedOrigins: List<String> = listOf("http://localhost:3000", "http://localhost:5173"),
)
