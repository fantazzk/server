package com.naminhyeok.fantazzk.template.repository

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@Configuration
@EnableJdbcRepositories(
    basePackageClasses = [
        TemplateRepositoryConfiguration::class,
    ],
)
class TemplateRepositoryConfiguration {
    @Bean
    fun templateRepository(templateJdbcCrudRepository: TemplateJdbcCrudRepository): TemplateRepository =
        TemplateRepositoryImpl(templateJdbcCrudRepository)

    @Bean
    fun templatePlayerRepository(templatePlayerJdbcCrudRepository: TemplatePlayerJdbcCrudRepository): TemplatePlayerRepository =
        TemplatePlayerRepositoryImpl(templatePlayerJdbcCrudRepository)
}
