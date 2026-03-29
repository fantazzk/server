package com.naminhyeok.fantazzk.integration.roomtemplate

import com.naminhyeok.fantazzk.room.outport.TemplateLookupPort
import com.naminhyeok.fantazzk.template.TemplateLookUpService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class RoomTemplateIntegrationAutoConfiguration {
    @Bean
    fun templateLookupPort(templateLookUpService: TemplateLookUpService): TemplateLookupPort = TemplateLookupAdapter(templateLookUpService)
}
