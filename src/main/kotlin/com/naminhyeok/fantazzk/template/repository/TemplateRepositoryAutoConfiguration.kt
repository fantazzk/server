package com.naminhyeok.fantazzk.template.repository

import com.naminhyeok.fantazzk.template.query.TemplatePlayerViewCrudRepository
import com.naminhyeok.fantazzk.template.query.TemplateViewCrudRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@Configuration
@EnableJdbcRepositories(
    basePackageClasses = [
        TemplateRepositoryAutoConfiguration::class,
        TemplateViewCrudRepository::class,
        TemplatePlayerViewCrudRepository::class,
    ],
)
class TemplateRepositoryAutoConfiguration {
    @Bean
    fun templateRepository(templateJdbcCrudRepository: TemplateJdbcCrudRepository): TemplateRepository =
        TemplateRepositoryImpl(templateJdbcCrudRepository)

    @Bean
    fun templatePlayerRepository(templatePlayerJdbcCrudRepository: TemplatePlayerJdbcCrudRepository): TemplatePlayerRepository =
        TemplatePlayerRepositoryImpl(templatePlayerJdbcCrudRepository)
}
