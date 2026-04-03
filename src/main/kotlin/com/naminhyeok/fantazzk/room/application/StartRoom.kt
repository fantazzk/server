package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.Rooms
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class StartRoom(
    private val roomRepository: Rooms,
) {
    @Transactional
    fun start(code: String) {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        roomRepository.save(room.start())
    }
}
