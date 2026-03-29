package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.outport.TemplateLookupPort
import com.naminhyeok.fantazzk.room.repository.RoomBidRepository
import com.naminhyeok.fantazzk.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamMemberRepository
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class RoomAutoConfiguration {
    @Bean
    fun roomLookUpService(
        roomRepository: RoomRepository,
        roomPlayerRepository: RoomPlayerRepository,
        roomTeamLeaderRepository: RoomTeamLeaderRepository,
        roomTeamMemberRepository: RoomTeamMemberRepository,
        roomBidRepository: RoomBidRepository,
    ): RoomLookUpService =
        RoomLookUpServiceImpl(roomRepository, roomPlayerRepository, roomTeamLeaderRepository, roomTeamMemberRepository, roomBidRepository)

    @Bean
    fun roomCreateService(
        roomRepository: RoomRepository,
        roomPlayerRepository: RoomPlayerRepository,
        roomTeamLeaderRepository: RoomTeamLeaderRepository,
        templateLookupPort: TemplateLookupPort,
    ): RoomCreateService = RoomCreateServiceImpl(roomRepository, roomPlayerRepository, roomTeamLeaderRepository, templateLookupPort)

    @Bean
    fun roomJoinService(
        roomRepository: RoomRepository,
        roomTeamLeaderRepository: RoomTeamLeaderRepository,
    ): RoomJoinService = RoomJoinServiceImpl(roomRepository, roomTeamLeaderRepository)

    @Bean
    fun roomStartService(
        roomRepository: RoomRepository,
        roomTeamLeaderRepository: RoomTeamLeaderRepository,
    ): RoomStartService = RoomStartServiceImpl(roomRepository, roomTeamLeaderRepository)

    @Bean
    fun auctionService(
        roomRepository: RoomRepository,
        roomTeamLeaderRepository: RoomTeamLeaderRepository,
        roomPlayerRepository: RoomPlayerRepository,
        roomTeamMemberRepository: RoomTeamMemberRepository,
        roomBidRepository: RoomBidRepository,
    ): AuctionService =
        AuctionServiceImpl(roomRepository, roomTeamLeaderRepository, roomPlayerRepository, roomTeamMemberRepository, roomBidRepository)

    @Bean
    fun draftService(
        roomRepository: RoomRepository,
        roomTeamLeaderRepository: RoomTeamLeaderRepository,
        roomPlayerRepository: RoomPlayerRepository,
        roomTeamMemberRepository: RoomTeamMemberRepository,
    ): DraftService = DraftServiceImpl(roomRepository, roomTeamLeaderRepository, roomPlayerRepository, roomTeamMemberRepository)
}
