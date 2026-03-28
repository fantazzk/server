package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.exception.RoomNotFoundException
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomTeamLeaderRepository

interface RoomStartService {
    fun start(code: String)
}

internal class RoomStartServiceImpl(
    private val roomRepository: RoomRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
) : RoomStartService {
    override fun start(code: String) {
        val room = roomRepository.findByCode(code) ?: throw RoomNotFoundException()
        check(room.status == RoomStatus.WAITING) { "대기 중인 방에서만 시작할 수 있습니다" }

        val leaders = roomTeamLeaderRepository.findByRoomId(room.roomId)
        check(leaders.size == room.teamCount) { "모든 팀장 자리가 채워져야 시작할 수 있습니다" }

        roomRepository.updateStatus(room.roomId, RoomStatus.IN_PROGRESS)
    }
}
