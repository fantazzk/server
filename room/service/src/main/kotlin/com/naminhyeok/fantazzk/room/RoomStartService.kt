package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository

interface RoomStartService {
    fun start(code: String)
}

internal class RoomStartServiceImpl(
    private val roomRepository: RoomRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
) : RoomStartService {
    override fun start(code: String) {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        check(room.isWaiting()) { "대기 중인 방에서만 시작할 수 있습니다" }

        val leaders = roomTeamLeaderRepository.findByRoomId(room.roomId)
        check(leaders.size == room.teamCount) { "모든 팀장 자리가 채워져야 시작할 수 있습니다" }

        val startedRoom =
            Room.from(room).copy(
                status = RoomStatus.IN_PROGRESS,
                currentAuctionRound = if (room.isAuction()) 1 else null,
                currentTurnIndex = if (room.isDraft()) 0 else null,
            )
        roomRepository.save(startedRoom)
    }
}
