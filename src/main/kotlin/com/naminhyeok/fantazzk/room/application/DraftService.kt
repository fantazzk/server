package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.RoomTeamMember
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DraftService(
    private val roomRepository: RoomRepository,
) {
    @Transactional
    fun pick(
        code: String,
        teamLeaderId: String,
        playerName: String,
    ): RoomTeamMember {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = roomRepository.save(room.pick(teamLeaderId = teamLeaderId, playerName = playerName))
        return savedRoom.members.last()
    }
}
