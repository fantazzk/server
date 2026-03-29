package com.naminhyeok.fantazzk.bootstrap.root

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration

@AutoConfiguration
class CombinedJdbcConfiguration(
    private val roomJdbcConverters: List<Converter<*, *>>,
    private val templateJdbcConverters: List<Converter<*, *>>,
) : AbstractJdbcConfiguration() {
    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions = JdbcCustomConversions(roomJdbcConverters + templateJdbcConverters)
}
