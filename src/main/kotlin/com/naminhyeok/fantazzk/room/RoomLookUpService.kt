package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.RoomBidModel
import com.naminhyeok.fantazzk.room.RoomModel
import com.naminhyeok.fantazzk.room.RoomPlayerModel
import com.naminhyeok.fantazzk.room.RoomTeamLeaderModel
import com.naminhyeok.fantazzk.room.RoomTeamMemberModel
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomBidRepository
import com.naminhyeok.fantazzk.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamMemberRepository
import org.springframework.stereotype.Service

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

@Service
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
