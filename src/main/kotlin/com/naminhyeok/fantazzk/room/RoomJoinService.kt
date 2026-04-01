package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.RoomTeamLeaderModel
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomAggregateRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface RoomJoinService {
    fun join(
        code: String,
        nickname: String,
    ): RoomTeamLeaderModel
}

@Service
internal class RoomJoinServiceImpl(
    private val roomAggregateRepository: RoomAggregateRepository,
    private val events: ApplicationEventPublisher,
) : RoomJoinService {
    @Transactional
    override fun join(
        code: String,
        nickname: String,
    ): RoomTeamLeaderModel {
        val room = roomAggregateRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = roomAggregateRepository.save(room.join(nickname))
        val leader = savedRoom.leaders.last()
        savedRoom.drainEvents().forEach(events::publishEvent)
        return leader
    }
}
