package com.naminhyeok.fantazzk.template.config

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter

@AutoConfiguration
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
