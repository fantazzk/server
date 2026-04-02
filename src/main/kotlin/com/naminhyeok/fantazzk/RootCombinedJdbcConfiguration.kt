package com.naminhyeok.fantazzk

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration

@Configuration("com.naminhyeok.fantazzk.RootCombinedJdbcConfiguration")
class RootCombinedJdbcConfiguration(
    private val roomJdbcConverters: List<Converter<*, *>>,
    private val templateJdbcConverters: List<Converter<*, *>>,
) : AbstractJdbcConfiguration() {
    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions = JdbcCustomConversions(roomJdbcConverters + templateJdbcConverters)
}
