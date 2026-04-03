package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.RoomTeamLeader
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@org.jmolecules.ddd.annotation.Service
@Service
class RoomJoinService(
    private val roomRepository: RoomRepository,
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
