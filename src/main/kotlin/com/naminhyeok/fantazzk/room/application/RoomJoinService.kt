package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.RoomTeamLeader
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface RoomJoinService {
    fun join(
        code: String,
        nickname: String,
    ): RoomTeamLeader
}

@org.jmolecules.ddd.annotation.Service
@Service
internal class RoomJoinServiceImpl(
    private val roomRepository: RoomRepository,
    private val events: ApplicationEventPublisher,
) : RoomJoinService {
    @Transactional
    override fun join(
        code: String,
        nickname: String,
    ): RoomTeamLeader {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = roomRepository.save(room.join(nickname))
        val leader = savedRoom.leaders.last()
        savedRoom.drainEvents().forEach(events::publishEvent)
        return leader
    }
}
