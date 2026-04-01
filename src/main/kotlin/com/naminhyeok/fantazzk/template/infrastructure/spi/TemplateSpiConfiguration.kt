package com.naminhyeok.fantazzk.template.infrastructure.spi

import com.naminhyeok.fantazzk.template.application.TemplateLookupService
import com.naminhyeok.fantazzk.template.spi.TemplateLookup
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TemplateSpiConfiguration {
    @Bean
    fun templateLookup(templateLookupService: TemplateLookupService): TemplateLookup = TemplateLookupAdapter(templateLookupService)
}
