package com.naminhyeok.fantazzk.bootstrap.template

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import java.time.Clock

@AutoConfiguration
class TemplateApplicationAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    @ConditionalOnMissingBean(OpenAPI::class)
    fun openApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Template API")
                    .version("v1")
                    .description("팀 빌딩 템플릿 관리"),
            )

    @Bean
    fun templateGroupedOpenApi(): GroupedOpenApi =
        GroupedOpenApi.builder()
            .group("template")
            .packagesToScan("com.naminhyeok.fantazzk.template")
            .build()
}
