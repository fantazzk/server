package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface RoomStartService {
    fun start(code: String)
}

@org.jmolecules.ddd.annotation.Service
@Service
internal open class RoomStartServiceImpl(
    private val roomRepository: RoomRepository,
    private val events: ApplicationEventPublisher,
) : RoomStartService {
    @Transactional
    override fun start(code: String) {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = roomRepository.save(room.start())
        savedRoom.drainEvents().forEach(events::publishEvent)
    }
}
