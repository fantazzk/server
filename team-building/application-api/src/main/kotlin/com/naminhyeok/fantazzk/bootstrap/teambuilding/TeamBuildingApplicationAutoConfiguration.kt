package com.naminhyeok.fantazzk.bootstrap.teambuilding

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock

@AutoConfiguration
class TeamBuildingApplicationAutoConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun openApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("TeamBuilding API")
                    .version("v1")
                    .description("팀 빌딩 경매/드래프트 플랫폼"),
            )
}
