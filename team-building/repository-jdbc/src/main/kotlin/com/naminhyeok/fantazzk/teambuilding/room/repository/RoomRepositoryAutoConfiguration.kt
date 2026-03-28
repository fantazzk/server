package com.naminhyeok.fantazzk.teambuilding.room.repository

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@AutoConfiguration
@EnableJdbcRepositories(basePackageClasses = [RoomRepositoryAutoConfiguration::class])
class RoomRepositoryAutoConfiguration {
    @Bean
    fun roomRepository(roomJdbcCrudRepository: RoomJdbcCrudRepository): RoomRepository = RoomRepositoryImpl(roomJdbcCrudRepository)

    @Bean
    fun roomPlayerRepository(roomPlayerJdbcCrudRepository: RoomPlayerJdbcCrudRepository): RoomPlayerRepository =
        RoomPlayerRepositoryImpl(roomPlayerJdbcCrudRepository)

    @Bean
    fun roomTeamLeaderRepository(roomTeamLeaderJdbcCrudRepository: RoomTeamLeaderJdbcCrudRepository): RoomTeamLeaderRepository =
        RoomTeamLeaderRepositoryImpl(roomTeamLeaderJdbcCrudRepository)

    @Bean
    fun roomTeamMemberRepository(roomTeamMemberJdbcCrudRepository: RoomTeamMemberJdbcCrudRepository): RoomTeamMemberRepository =
        RoomTeamMemberRepositoryImpl(roomTeamMemberJdbcCrudRepository)

    @Bean
    fun roomBidRepository(roomBidJdbcCrudRepository: RoomBidJdbcCrudRepository): RoomBidRepository =
        RoomBidRepositoryImpl(roomBidJdbcCrudRepository)
}
