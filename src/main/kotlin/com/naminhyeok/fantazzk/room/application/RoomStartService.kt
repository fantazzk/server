package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@org.jmolecules.ddd.annotation.Service
@Service
class RoomStartService(
    private val roomRepository: RoomRepository,
) {
    @Transactional
    fun start(code: String) {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        roomRepository.save(room.start())
    }
}
