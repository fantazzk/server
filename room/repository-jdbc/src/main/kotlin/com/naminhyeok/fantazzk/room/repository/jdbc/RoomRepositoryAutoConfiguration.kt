package com.naminhyeok.fantazzk.room.repository.jdbc

import com.naminhyeok.fantazzk.room.infrastructure.RoomBidRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomTeamMemberRepository
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
