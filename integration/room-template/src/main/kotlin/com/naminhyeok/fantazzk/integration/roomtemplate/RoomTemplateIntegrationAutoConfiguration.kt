package com.naminhyeok.fantazzk.integration.roomtemplate

import com.naminhyeok.fantazzk.room.outport.TemplateLookupPort
import com.naminhyeok.fantazzk.template.TemplateLookupService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class RoomTemplateIntegrationAutoConfiguration {
    @Bean
    fun templateLookupPort(templateLookupService: TemplateLookupService): TemplateLookupPort = TemplateLookupAdapter(templateLookupService)
}
