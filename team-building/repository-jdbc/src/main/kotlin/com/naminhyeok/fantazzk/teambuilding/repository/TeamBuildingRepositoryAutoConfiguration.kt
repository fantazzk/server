package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.room.AuctionResult
import com.naminhyeok.fantazzk.teambuilding.room.Progression
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.simple.JdbcClient
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper

@AutoConfiguration
class TeamBuildingRepositoryAutoConfiguration {
    @Bean
    fun teamBuildingObjectMapper(baseMapper: ObjectMapper): ObjectMapper {
        val builder = (baseMapper as JsonMapper).rebuild()
        builder.addMixIn(Progression::class.java, ProgressionMixin::class.java)
        builder.addMixIn(AuctionResult.Outcome::class.java, AuctionResultOutcomeMixin::class.java)
        return builder.build()
    }

    @Bean
    fun templateRepository(
        jdbcClient: JdbcClient,
        teamBuildingObjectMapper: ObjectMapper,
    ): TemplateRepository = TemplateRepositoryImpl(jdbcClient, teamBuildingObjectMapper)

    @Bean
    fun roomRepository(
        jdbcClient: JdbcClient,
        teamBuildingObjectMapper: ObjectMapper,
    ): RoomRepository = RoomRepositoryImpl(jdbcClient, teamBuildingObjectMapper)
}
