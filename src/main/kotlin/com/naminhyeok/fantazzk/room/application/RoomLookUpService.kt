package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomBid
import com.naminhyeok.fantazzk.room.RoomPlayer
import com.naminhyeok.fantazzk.room.RoomTeamLeader
import com.naminhyeok.fantazzk.room.RoomTeamMember
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomBidRepository
import com.naminhyeok.fantazzk.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamMemberRepository
import org.springframework.stereotype.Service

interface RoomLookupService {
    fun get(code: String): Room

    fun getPlayers(roomId: Long): List<RoomPlayer>

    fun getTeamLeaders(roomId: Long): List<RoomTeamLeader>

    fun getTeamMembers(roomId: Long): List<RoomTeamMember>

    fun getBids(
        roomId: Long,
        round: Int,
    ): List<RoomBid>
}

@org.jmolecules.ddd.annotation.Service
@Service
internal class RoomLookupServiceImpl(
    private val roomRepository: RoomRepository,
    private val roomPlayerRepository: RoomPlayerRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
    private val roomTeamMemberRepository: RoomTeamMemberRepository,
    private val roomBidRepository: RoomBidRepository,
) : RoomLookupService {
    override fun get(code: String): Room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()

    override fun getPlayers(roomId: Long): List<RoomPlayer> = roomPlayerRepository.findByRoomId(roomId)

    override fun getTeamLeaders(roomId: Long): List<RoomTeamLeader> = roomTeamLeaderRepository.findByRoomId(roomId)

    override fun getTeamMembers(roomId: Long): List<RoomTeamMember> = roomTeamMemberRepository.findByRoomId(roomId)

    override fun getBids(
        roomId: Long,
        round: Int,
    ): List<RoomBid> = roomBidRepository.findByRoomIdAndRound(roomId, round)
}
