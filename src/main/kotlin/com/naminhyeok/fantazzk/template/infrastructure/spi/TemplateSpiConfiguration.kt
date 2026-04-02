package com.naminhyeok.fantazzk.template.infrastructure.spi

import com.naminhyeok.fantazzk.template.application.TemplateFinder
import com.naminhyeok.fantazzk.template.spi.TemplateLookup
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TemplateSpiConfiguration {
    @Bean
    fun templateLookup(templateFinder: TemplateFinder): TemplateLookup = TemplateLookupAdapter(templateFinder)
}
