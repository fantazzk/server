package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.domain.RoomTeamMember
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.Rooms
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PickDraft(
    private val roomRepository: Rooms,
) {
    @Transactional
    fun pick(
        code: String,
        teamLeaderId: String,
        playerName: String,
    ): RoomTeamMember {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = roomRepository.save(room.pick(teamLeaderId, playerName))
        return savedRoom.members.last()
    }
}
