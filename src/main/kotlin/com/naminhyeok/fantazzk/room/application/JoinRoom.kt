package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.Rooms
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class JoinRoom(
    private val roomRepository: Rooms,
) {
    @Transactional
    fun join(
        code: String,
        nickname: String,
    ): RoomTeamLeader {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = roomRepository.save(room.join(nickname))
        val leader = savedRoom.leaders.last()
        return leader
    }
}
