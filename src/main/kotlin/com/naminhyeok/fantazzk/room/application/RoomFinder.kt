package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.stereotype.Component

@Component
class RoomFinder(
    private val roomRepository: RoomRepository,
) {
    fun get(code: String): Room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
}
