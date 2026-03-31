package com.naminhyeok.fantazzk.template.repository.jdbc

import com.naminhyeok.fantazzk.template.infrastructure.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.infrastructure.TemplateRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@AutoConfiguration
@EnableJdbcRepositories(basePackageClasses = [TemplateRepositoryAutoConfiguration::class])
class TemplateRepositoryAutoConfiguration {
    @Bean
    fun templateRepository(templateJdbcCrudRepository: TemplateJdbcCrudRepository): TemplateRepository =
        TemplateRepositoryImpl(templateJdbcCrudRepository)

    @Bean
    fun templatePlayerRepository(templatePlayerJdbcCrudRepository: TemplatePlayerJdbcCrudRepository): TemplatePlayerRepository =
        TemplatePlayerRepositoryImpl(templatePlayerJdbcCrudRepository)
}
