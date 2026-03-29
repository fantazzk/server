package com.naminhyeok.fantazzk.room.repository

import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration

@SpringBootConfiguration
class TestApplication(
    private val roomJdbcConverters: List<Converter<*, *>>,
) : AbstractJdbcConfiguration() {
    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions = JdbcCustomConversions(roomJdbcConverters)
}
