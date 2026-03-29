package com.naminhyeok.fantazzk.template.config

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration

@AutoConfiguration
class TemplateJdbcConfiguration : AbstractJdbcConfiguration() {
    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions =
        JdbcCustomConversions(
            listOf(
                EnumToStringConverter(TeamBuildingMode::class.java),
                StringToEnumConverter(TeamBuildingMode::class.java),
                EnumToStringConverter(DraftOrderStrategy::class.java),
                StringToEnumConverter(DraftOrderStrategy::class.java),
            ),
        )
}
