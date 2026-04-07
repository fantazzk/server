package com.naminhyeok.fantazzk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SwaggerSecurityConfiguration {
    @Bean
    @ConditionalOnProperty("swagger.username")
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").authenticated()
                .anyRequest().permitAll())
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .build();
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .build();
    }

    @Bean
    @ConditionalOnProperty("swagger.username")
    InMemoryUserDetailsManager swaggerUserDetailsService(
        @Value("${swagger.username}") String username,
        @Value("${swagger.password}") String password
    ) {
        var user = User.builder()
            .username(username)
            .password("{noop}" + password)
            .roles("SWAGGER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
