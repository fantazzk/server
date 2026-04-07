package com.naminhyeok.fantazzk;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import java.time.Clock;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class FantazzkApplicationConfiguration {
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("Fantazzk API")
                    .version("v1")
                    .description(
                        """
                        Fantazzk의 드래프트/경매 팀 빌딩 API입니다.
                        모든 응답은 공통 envelope를 사용합니다.
                        - 성공 응답: resultType=SUCCESS, success 에 실제 payload 가 들어갑니다.
                        - 실패 응답: resultType=ERROR, error 에 status, errorCode, reason, data 가 들어갑니다.
                        """.trim()
                    )
            )
            .tags(
                List.of(
                    new Tag().name("Template").description("팀 빌딩 템플릿 생성 및 조회 API"),
                    new Tag().name("Room").description("방 생성, 참가, 시작, 경매, 드래프트 진행 API")
                )
            );
    }

    @Bean
    WebMvcConfigurer corsConfigurer(CorsProperties corsProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins(corsProperties.allowedOrigins().toArray(String[]::new))
                    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
