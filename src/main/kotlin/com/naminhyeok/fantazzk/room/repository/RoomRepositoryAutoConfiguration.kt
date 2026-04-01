package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.query.RoomViewCrudRepository
import com.naminhyeok.fantazzk.room.query.TeamLeaderViewCrudRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@Configuration
@EnableJdbcRepositories(
    basePackageClasses = [
        RoomRepositoryAutoConfiguration::class,
        RoomViewCrudRepository::class,
        TeamLeaderViewCrudRepository::class,
    ],
)
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
