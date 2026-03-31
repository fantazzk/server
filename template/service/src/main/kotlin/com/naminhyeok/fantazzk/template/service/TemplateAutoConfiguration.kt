package com.naminhyeok.fantazzk.template.service

import com.naminhyeok.fantazzk.template.api.TemplateLookup
import com.naminhyeok.fantazzk.template.infrastructure.TemplatePlayerRepository
import com.naminhyeok.fantazzk.template.infrastructure.TemplateRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class TemplateAutoConfiguration {
    @Bean
    fun templateLookupService(
        templateRepository: TemplateRepository,
        templatePlayerRepository: TemplatePlayerRepository,
    ): TemplateLookupService = TemplateLookupServiceImpl(templateRepository, templatePlayerRepository)

    @Bean
    fun templateCreateService(
        templateRepository: TemplateRepository,
        templatePlayerRepository: TemplatePlayerRepository,
    ): TemplateCreateService = TemplateCreateServiceImpl(templateRepository, templatePlayerRepository)

    @Bean
    fun templateLookup(templateLookupService: TemplateLookupService): TemplateLookup = TemplateLookupFacade(templateLookupService)
}
