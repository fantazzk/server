package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomAggregateRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface RoomStartService {
    fun start(code: String)
}

@Service
internal open class RoomStartServiceImpl(
    private val roomAggregateRepository: RoomAggregateRepository,
    private val events: ApplicationEventPublisher,
) : RoomStartService {
    @Transactional
    override fun start(code: String) {
        val room = roomAggregateRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        val savedRoom = roomAggregateRepository.save(room.start())
        savedRoom.drainEvents().forEach(events::publishEvent)
    }
}
