package com.naminhyeok.fantazzk

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SwaggerSecurityConfiguration {
    @Bean
    @ConditionalOnProperty("swagger.username")
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").authenticated()
                    .anyRequest().permitAll()
            }
            .httpBasic(Customizer.withDefaults())
            .csrf { it.disable() }
            .build()

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain::class)
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
