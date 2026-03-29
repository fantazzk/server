package com.naminhyeok.fantazzk.bootstrap.room

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.tags.Tag
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
                    .description(
                        """
                        팀 빌딩 방 생성과 진행 상태 변경을 위한 API입니다.
                        모든 응답은 resultType, success, error 로 구성된 공통 envelope를 사용합니다.
                        """.trimIndent(),
                    ),
            )
            .tags(listOf(Tag().name("Room").description("방 생성, 참가, 시작, 경매, 드래프트 진행 API")))

    @Bean
    fun roomGroupedOpenApi(): GroupedOpenApi =
        GroupedOpenApi.builder()
            .group("room")
            .packagesToScan("com.naminhyeok.fantazzk.room")
            .build()
}
