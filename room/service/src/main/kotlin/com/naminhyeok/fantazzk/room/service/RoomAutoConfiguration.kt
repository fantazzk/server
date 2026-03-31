package com.naminhyeok.fantazzk.room.service

import com.naminhyeok.fantazzk.room.infrastructure.RoomBidRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomTeamMemberRepository
import com.naminhyeok.fantazzk.template.api.TemplateLookup
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean

@AutoConfiguration
class RoomAutoConfiguration {
    @Bean
    fun roomLookupService(
        roomRepository: RoomRepository,
        roomPlayerRepository: RoomPlayerRepository,
        roomTeamLeaderRepository: RoomTeamLeaderRepository,
        roomTeamMemberRepository: RoomTeamMemberRepository,
        roomBidRepository: RoomBidRepository,
    ): RoomLookupService =
        RoomLookupServiceImpl(roomRepository, roomPlayerRepository, roomTeamLeaderRepository, roomTeamMemberRepository, roomBidRepository)

    @Bean
    fun roomCreateService(
        roomRepository: RoomRepository,
        roomPlayerRepository: RoomPlayerRepository,
        roomTeamLeaderRepository: RoomTeamLeaderRepository,
        templateLookup: TemplateLookup,
    ): RoomCreateService = RoomCreateServiceImpl(roomRepository, roomPlayerRepository, roomTeamLeaderRepository, templateLookup)

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
