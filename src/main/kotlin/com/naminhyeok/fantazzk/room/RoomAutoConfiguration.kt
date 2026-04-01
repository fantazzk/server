package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.repository.RoomAggregateRepositoryImpl
import com.naminhyeok.fantazzk.room.repository.RoomBidRepository
import com.naminhyeok.fantazzk.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamMemberRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RoomAutoConfiguration {
    @Bean
    internal fun roomAggregateRepository(
        roomRepository: RoomRepository,
        roomPlayerRepository: RoomPlayerRepository,
        roomTeamLeaderRepository: RoomTeamLeaderRepository,
        roomTeamMemberRepository: RoomTeamMemberRepository,
        roomBidRepository: RoomBidRepository,
    ): com.naminhyeok.fantazzk.room.repository.RoomAggregateRepository =
        RoomAggregateRepositoryImpl(
            roomRepository,
            roomPlayerRepository,
            roomTeamLeaderRepository,
            roomTeamMemberRepository,
            roomBidRepository,
        )
}
