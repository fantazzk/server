package com.naminhyeok.fantazzk.bootstrap.root

import com.naminhyeok.fantazzk.bootstrap.root.adapter.TemplateFetcherAdapter
import com.naminhyeok.fantazzk.room.outport.TemplateFetcher
import com.naminhyeok.fantazzk.template.TemplateLookUpService
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Clock

@AutoConfiguration
@EnableConfigurationProperties(CorsProperties::class)
class FantazzkApplicationAutoConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun openApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Fantazzk API")
                    .version("v1")
                    .description("Team building through draft & auction"),
            )

    @Bean
    fun corsConfigurer(corsProperties: CorsProperties): WebMvcConfigurer =
        object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins(*corsProperties.allowedOrigins.toTypedArray())
                    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
            }
        }

    @Bean
    fun templateFetcher(templateLookUpService: TemplateLookUpService): TemplateFetcher = TemplateFetcherAdapter(templateLookUpService)
}
