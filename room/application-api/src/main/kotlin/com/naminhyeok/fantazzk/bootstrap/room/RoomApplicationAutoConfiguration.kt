package com.naminhyeok.fantazzk.bootstrap.room

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import java.time.Clock

@AutoConfiguration
class RoomApplicationAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    @ConditionalOnMissingBean(OpenAPI::class)
    fun openApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Room API")
                    .version("v1")
                    .description("팀 빌딩 방 관리"),
            )

    @Bean
    fun roomGroupedOpenApi(): GroupedOpenApi =
        GroupedOpenApi.builder()
            .group("room")
            .packagesToScan("com.naminhyeok.fantazzk.room")
            .build()
}
