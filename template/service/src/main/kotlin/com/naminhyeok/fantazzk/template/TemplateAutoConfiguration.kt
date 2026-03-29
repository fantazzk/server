package com.naminhyeok.fantazzk.template

import com.naminhyeok.fantazzk.template.repository.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.repository.TemplateRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class TemplateAutoConfiguration {
    @Bean
    fun templateLookUpService(
        templateRepository: TemplateRepository,
        templatePlayerRepository: TemplatePlayerRepository,
    ): TemplateLookUpService = TemplateLookUpServiceImpl(templateRepository, templatePlayerRepository)

    @Bean
    fun templateCreateService(
        templateRepository: TemplateRepository,
        templatePlayerRepository: TemplatePlayerRepository,
    ): TemplateCreateService = TemplateCreateServiceImpl(templateRepository, templatePlayerRepository)
}
