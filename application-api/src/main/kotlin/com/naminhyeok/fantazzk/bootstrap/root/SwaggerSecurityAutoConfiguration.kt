package com.naminhyeok.fantazzk.bootstrap.root

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@AutoConfiguration
class SwaggerSecurityAutoConfiguration {
    @Bean
    @Order(1)
    @ConditionalOnProperty("swagger.username")
    fun swaggerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .securityMatcher("/swagger-ui/**", "/v3/api-docs/**")
            .authorizeHttpRequests { it.anyRequest().authenticated() }
            .httpBasic(Customizer.withDefaults())
            .csrf { it.disable() }
            .build()

    @Bean
    @Order(2)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .csrf { it.disable() }
            .build()

    @Bean
    @ConditionalOnProperty("swagger.username")
    fun swaggerUserDetailsService(
        @Value("\${swagger.username}") username: String,
        @Value("\${swagger.password}") password: String,
    ): InMemoryUserDetailsManager {
        val user =
            User.builder()
                .username(username)
                .password("{noop}$password")
                .roles("SWAGGER")
                .build()
        return InMemoryUserDetailsManager(user)
    }
}
