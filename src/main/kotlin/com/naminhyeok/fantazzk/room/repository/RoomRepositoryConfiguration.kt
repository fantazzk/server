package com.naminhyeok.fantazzk.room.repository

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.context.annotation.FilterType

@Configuration
@EnableJdbcRepositories(
    basePackageClasses = [
        RoomRepositoryConfiguration::class,
    ],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [RoomJpaStore::class],
        ),
    ],
)
class RoomRepositoryConfiguration {
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
