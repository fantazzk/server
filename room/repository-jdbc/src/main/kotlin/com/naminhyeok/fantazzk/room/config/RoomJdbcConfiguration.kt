package com.naminhyeok.fantazzk.room.config

import com.naminhyeok.fantazzk.room.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.PlayerStatus
import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.TeamBuildingMode
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter

@AutoConfiguration
class RoomJdbcConfiguration {
    @Bean
    fun roomJdbcConverters(): List<Converter<*, *>> =
        listOf(
            EnumToStringConverter(TeamBuildingMode::class.java),
            StringToEnumConverter(TeamBuildingMode::class.java),
            EnumToStringConverter(DraftOrderStrategy::class.java),
            StringToEnumConverter(DraftOrderStrategy::class.java),
            EnumToStringConverter(RoomStatus::class.java),
            StringToEnumConverter(RoomStatus::class.java),
            EnumToStringConverter(PlayerStatus::class.java),
            StringToEnumConverter(PlayerStatus::class.java),
        )
}
