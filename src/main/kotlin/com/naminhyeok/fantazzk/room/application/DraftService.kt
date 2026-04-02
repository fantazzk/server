package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.RoomTeamMember
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface DraftService {
    fun pick(
        code: String,
        teamLeaderId: String,
        playerName: String,
    ): RoomTeamMember
}

@org.jmolecules.ddd.annotation.Service
@Service
internal open class DraftServiceImpl(
    private val roomRepository: RoomRepository,
    private val events: ApplicationEventPublisher,
) : DraftService {
    @Transactional
    override fun pick(
        code: String,
        teamLeaderId: String,
        playerName: String,
    ): RoomTeamMember {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = roomRepository.save(room.pick(teamLeaderId = teamLeaderId, playerName = playerName))
        savedRoom.drainEvents().forEach(events::publishEvent)
        return savedRoom.members.last()
    }
}
