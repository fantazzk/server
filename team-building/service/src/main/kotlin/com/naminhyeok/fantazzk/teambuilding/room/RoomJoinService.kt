package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.exception.RoomNotFoundException
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomTeamLeaderRepository
import java.util.UUID

interface RoomJoinService {
    fun join(
        code: String,
        nickname: String,
    ): RoomTeamLeaderModel
}

internal class RoomJoinServiceImpl(
    private val roomRepository: RoomRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
) : RoomJoinService {
    override fun join(
        code: String,
        nickname: String,
    ): RoomTeamLeaderModel {
        val room = roomRepository.findByCode(code) ?: throw RoomNotFoundException()
        check(room.status == RoomStatus.WAITING) { "대기 중인 방에서만 참가할 수 있습니다" }

        val currentLeaders = roomTeamLeaderRepository.findByRoomId(room.roomId)
        check(currentLeaders.size < room.teamCount) { "방이 가득 찼습니다" }

        return roomTeamLeaderRepository.save(
            RoomTeamLeader(
                roomId = room.roomId,
                teamLeaderId = UUID.randomUUID().toString(),
                nickname = nickname,
                remainingBudget = room.budget,
            ),
        )
    }
}
