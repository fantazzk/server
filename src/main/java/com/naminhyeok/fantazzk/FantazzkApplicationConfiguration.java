package com.naminhyeok.fantazzk;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.time.Clock;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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
    TaskScheduler taskScheduler(Clock clock) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("room-auction-");
        scheduler.setClock(clock);
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
            .components(
                new Components().addSecuritySchemes(
                    OpenApiDocumentation.ROOM_ACTION_TOKEN_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name(OpenApiDocumentation.ROOM_ACTION_TOKEN_HEADER)
                        .description(OpenApiDocumentation.ROOM_ACTION_TOKEN_DESCRIPTION)
                )
            )
            .info(
                new Info()
                    .title("Fantazzk API")
                    .version("v1")
                    .description(OpenApiDocumentation.OPENAPI_DESCRIPTION)
            )
            .tags(
                List.of(
                    new Tag()
                        .name(OpenApiDocumentation.TEMPLATE_TAG)
                        .description("템플릿 조회/생성 API. 웹에서 첫 진입 시 사용할 룰과 선수 풀을 확인합니다."),
                    new Tag()
                        .name(OpenApiDocumentation.ROOM_SESSION_TAG)
                        .description("방 생성/참가 API. 성공 응답에서 `actionToken` 을 받아 이후 mutation 요청에 사용합니다."),
                    new Tag()
                        .name(OpenApiDocumentation.ROOM_LOBBY_TAG)
                        .description("대기방 로비 조회 및 시작 전 액션 API. `startedGameId` 가 생기면 Game Play API 로 전환합니다."),
                    new Tag()
                        .name(OpenApiDocumentation.GAME_PLAY_TAG)
                        .description("방 시작 후 진행 화면용 API. 시작 이후 상태의 source of truth 입니다.")
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
