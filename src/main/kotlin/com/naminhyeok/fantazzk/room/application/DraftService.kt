package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.RoomTeamMember
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomAggregateRepository
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
    private val roomAggregateRepository: RoomAggregateRepository,
    private val events: ApplicationEventPublisher,
) : DraftService {
    @Transactional
    override fun pick(
        code: String,
        teamLeaderId: String,
        playerName: String,
    ): RoomTeamMember {
        val room = roomAggregateRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = roomAggregateRepository.save(room.pick(teamLeaderId = teamLeaderId, playerName = playerName))
        savedRoom.drainEvents().forEach(events::publishEvent)
        return savedRoom.members.last()
    }
}
