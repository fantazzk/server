package com.naminhyeok.fantazzk.teambuilding.repository

import com.naminhyeok.fantazzk.teambuilding.room.AuctionResult
import com.naminhyeok.fantazzk.teambuilding.room.Progression
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper

@AutoConfiguration
@EnableJdbcRepositories
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
        templateJdbcRepository: TemplateJdbcRepository,
        teamBuildingObjectMapper: ObjectMapper,
    ): TemplateRepository = TemplateRepositoryImpl(templateJdbcRepository, teamBuildingObjectMapper)

    @Bean
    fun roomRepository(
        roomJdbcRepository: RoomJdbcRepository,
        teamBuildingObjectMapper: ObjectMapper,
    ): RoomRepository = RoomRepositoryImpl(roomJdbcRepository, teamBuildingObjectMapper)
}
