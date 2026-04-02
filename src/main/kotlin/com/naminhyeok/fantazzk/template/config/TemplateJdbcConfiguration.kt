package com.naminhyeok.fantazzk.template.config

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter

@Configuration
class TemplateJdbcConfiguration {
    @Bean
    fun templateJdbcConverters(): List<Converter<*, *>> =
        listOf(
            EnumToStringConverter(TeamBuildingMode::class.java),
            StringToEnumConverter(TeamBuildingMode::class.java),
            EnumToStringConverter(DraftOrderStrategy::class.java),
            StringToEnumConverter(DraftOrderStrategy::class.java),
        )
}
