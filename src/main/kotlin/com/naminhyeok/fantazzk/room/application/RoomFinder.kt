package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.stereotype.Service

interface RoomFinder {
    fun get(code: String): Room
}

@org.jmolecules.ddd.annotation.Service
@Service
internal class RoomFinderImpl(
    private val roomRepository: RoomRepository,
) : RoomFinder {
    override fun get(code: String): Room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
}
