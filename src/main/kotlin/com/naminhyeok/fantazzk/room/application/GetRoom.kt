package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.domain.Room
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.Rooms
import org.springframework.stereotype.Component

@Component
class GetRoom(
    private val roomRepository: Rooms,
) {
    fun get(code: String): Room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
}
