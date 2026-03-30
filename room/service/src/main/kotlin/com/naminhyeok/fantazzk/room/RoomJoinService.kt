package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository
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
        val room = Room.from(roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException())
        val currentLeaders = roomTeamLeaderRepository.findByRoomId(room.roomId)

        return roomTeamLeaderRepository.save(
            room.join(
                teamLeaderId = UUID.randomUUID().toString(),
                nickname = nickname,
                currentLeaderCount = currentLeaders.size,
            ),
        )
    }
}
