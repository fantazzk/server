package com.naminhyeok.fantazzk.teambuilding.config

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.TeamBuildingMode
import com.naminhyeok.fantazzk.teambuilding.room.PlayerStatus
import com.naminhyeok.fantazzk.teambuilding.room.RoomStatus
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration

@AutoConfiguration
class TeamBuildingJdbcConfiguration : AbstractJdbcConfiguration() {
    @Bean
    override fun jdbcCustomConversions(): JdbcCustomConversions =
        JdbcCustomConversions(
            listOf(
                EnumToStringConverter(TeamBuildingMode::class.java),
                StringToEnumConverter(TeamBuildingMode::class.java),
                EnumToStringConverter(DraftOrderStrategy::class.java),
                StringToEnumConverter(DraftOrderStrategy::class.java),
                EnumToStringConverter(RoomStatus::class.java),
                StringToEnumConverter(RoomStatus::class.java),
                EnumToStringConverter(PlayerStatus::class.java),
                StringToEnumConverter(PlayerStatus::class.java),
            ),
        )
}
