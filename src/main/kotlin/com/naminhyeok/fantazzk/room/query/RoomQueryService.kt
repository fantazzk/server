package com.naminhyeok.fantazzk.room.query

import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.exception.RoomException
import org.springframework.stereotype.Service

data class RoomView(
    val roomId: Long,
    val code: String,
    val status: RoomStatus,
    val teamLeaders: List<TeamLeaderView>,
)

data class TeamLeaderView(
    val id: String,
    val nickname: String,
    val remainingBudget: Int?,
)

interface RoomQueryService {
    fun getRoom(code: String): RoomView
}

@org.jmolecules.ddd.annotation.Service
@Service
internal class RoomQueryServiceImpl(
    private val roomViewProjectionRepository: RoomViewProjectionRepository,
    private val teamLeaderViewProjectionRepository: TeamLeaderViewProjectionRepository,
) : RoomQueryService {
    override fun getRoom(code: String): RoomView {
        val room = roomViewProjectionRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val leaders =
            teamLeaderViewProjectionRepository.findByRoomIdOrderById(room.roomId).map {
                TeamLeaderView(
                    id = it.teamLeaderId,
                    nickname = it.nickname,
                    remainingBudget = it.remainingBudget,
                )
            }

        return RoomView(
            roomId = room.roomId,
            code = room.code,
            status = room.status,
            teamLeaders = leaders,
        )
    }
}
