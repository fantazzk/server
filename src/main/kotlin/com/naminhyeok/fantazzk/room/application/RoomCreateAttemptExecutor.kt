package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@org.jmolecules.ddd.annotation.Service
@Service
class RoomCreateAttemptExecutor(
    private val roomRepository: RoomRepository,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun create(room: Room): Room {
        return roomRepository.save(room)
    }
}
