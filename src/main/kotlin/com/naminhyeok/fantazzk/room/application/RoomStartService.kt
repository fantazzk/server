package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RoomStartService(
    private val roomRepository: RoomRepository,
) {
    @Transactional
    fun start(code: String) {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        roomRepository.save(room.start())
    }
}
