package com.naminhyeok.fantazzk.room.service

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.infrastructure.RoomBidRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomTeamMemberRepository
import com.naminhyeok.fantazzk.room.model.RoomBidModel
import com.naminhyeok.fantazzk.room.model.RoomModel
import com.naminhyeok.fantazzk.room.model.RoomPlayerModel
import com.naminhyeok.fantazzk.room.model.RoomTeamLeaderModel
import com.naminhyeok.fantazzk.room.model.RoomTeamMemberModel

interface RoomLookupService {
    fun get(code: String): RoomModel

    fun getPlayers(roomId: Long): List<RoomPlayerModel>

    fun getTeamLeaders(roomId: Long): List<RoomTeamLeaderModel>

    fun getTeamMembers(roomId: Long): List<RoomTeamMemberModel>

    fun getBids(
        roomId: Long,
        round: Int,
    ): List<RoomBidModel>
}

internal class RoomLookupServiceImpl(
    private val roomRepository: RoomRepository,
    private val roomPlayerRepository: RoomPlayerRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
    private val roomTeamMemberRepository: RoomTeamMemberRepository,
    private val roomBidRepository: RoomBidRepository,
) : RoomLookupService {
    override fun get(code: String): RoomModel = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()

    override fun getPlayers(roomId: Long): List<RoomPlayerModel> = roomPlayerRepository.findByRoomId(roomId)

    override fun getTeamLeaders(roomId: Long): List<RoomTeamLeaderModel> = roomTeamLeaderRepository.findByRoomId(roomId)

    override fun getTeamMembers(roomId: Long): List<RoomTeamMemberModel> = roomTeamMemberRepository.findByRoomId(roomId)

    override fun getBids(
        roomId: Long,
        round: Int,
    ): List<RoomBidModel> = roomBidRepository.findByRoomIdAndRound(roomId, round)
}
