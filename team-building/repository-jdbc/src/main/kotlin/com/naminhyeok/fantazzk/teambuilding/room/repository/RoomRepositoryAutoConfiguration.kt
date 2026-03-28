package com.naminhyeok.fantazzk.teambuilding.room.repository

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.jdbc.core.simple.JdbcClient

@AutoConfiguration
@EnableJdbcRepositories(basePackageClasses = [RoomRepositoryAutoConfiguration::class])
class RoomRepositoryAutoConfiguration {
    @Bean
    fun roomRepository(
        roomJdbcCrudRepository: RoomJdbcCrudRepository,
        jdbcClient: JdbcClient,
    ): RoomRepository = RoomRepositoryImpl(roomJdbcCrudRepository, jdbcClient)

    @Bean
    fun roomPlayerRepository(
        roomPlayerJdbcCrudRepository: RoomPlayerJdbcCrudRepository,
        jdbcClient: JdbcClient,
    ): RoomPlayerRepository = RoomPlayerRepositoryImpl(roomPlayerJdbcCrudRepository, jdbcClient)

    @Bean
    fun roomTeamLeaderRepository(
        roomTeamLeaderJdbcCrudRepository: RoomTeamLeaderJdbcCrudRepository,
        jdbcClient: JdbcClient,
    ): RoomTeamLeaderRepository = RoomTeamLeaderRepositoryImpl(roomTeamLeaderJdbcCrudRepository, jdbcClient)

    @Bean
    fun roomTeamMemberRepository(roomTeamMemberJdbcCrudRepository: RoomTeamMemberJdbcCrudRepository): RoomTeamMemberRepository =
        RoomTeamMemberRepositoryImpl(roomTeamMemberJdbcCrudRepository)

    @Bean
    fun roomBidRepository(roomBidJdbcCrudRepository: RoomBidJdbcCrudRepository): RoomBidRepository =
        RoomBidRepositoryImpl(roomBidJdbcCrudRepository)
}
