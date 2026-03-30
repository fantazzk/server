package com.naminhyeok.fantazzk.bootstrap.template

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.tags.Tag
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
                    .description(
                        """
                        팀 빌딩 템플릿 생성과 조회를 위한 API입니다.
                        모든 응답은 resultType, success, error 로 구성된 공통 envelope를 사용합니다.
                        """.trimIndent(),
                    ),
            )
            .tags(listOf(Tag().name("Template").description("팀 빌딩 템플릿 생성 및 조회 API")))

    @Bean
    fun templateGroupedOpenApi(): GroupedOpenApi =
        GroupedOpenApi.builder()
            .group("template")
            .packagesToScan("com.naminhyeok.fantazzk.template")
            .build()
}
