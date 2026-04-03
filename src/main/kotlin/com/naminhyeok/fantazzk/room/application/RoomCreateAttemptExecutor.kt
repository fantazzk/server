package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.domain.Room
import com.naminhyeok.fantazzk.room.repository.Rooms
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class RoomCreateAttemptExecutor(
    private val roomRepository: Rooms,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun create(room: Room): Room {
        return roomRepository.save(room)
    }
}
