package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

internal interface RoomCreateAttemptExecutor {
    fun create(room: Room): Room
}

@org.jmolecules.ddd.annotation.Service
@Service
internal class RoomCreateAttemptExecutorImpl(
    private val roomRepository: RoomRepository,
    private val events: ApplicationEventPublisher,
) : RoomCreateAttemptExecutor {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun create(room: Room): Room {
        val savedRoom = roomRepository.save(room)
        savedRoom.recordCreated().drainEvents().forEach(events::publishEvent)
        return savedRoom
    }
}
