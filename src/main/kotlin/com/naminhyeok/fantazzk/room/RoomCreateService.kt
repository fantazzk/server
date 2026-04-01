package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomModel
import com.naminhyeok.fantazzk.room.exception.RoomTemplateNotFoundException
import com.naminhyeok.fantazzk.room.repository.RoomAggregateRepository
import com.naminhyeok.fantazzk.template.spi.TemplateLookup
import com.naminhyeok.fantazzk.template.spi.TemplateLookupException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface RoomCreateService {
    fun create(
        templateId: Long,
        hostNickname: String,
    ): RoomModel
}

@Service
internal class RoomCreateServiceImpl(
    private val roomAggregateRepository: RoomAggregateRepository,
    private val templateLookup: TemplateLookup,
    private val events: ApplicationEventPublisher,
) : RoomCreateService {
    @Transactional
    override fun create(
        templateId: Long,
        hostNickname: String,
    ): RoomModel {
        val template =
            try {
                templateLookup.getTemplate(templateId)
            } catch (_: TemplateLookupException.NotFound) {
                throw RoomTemplateNotFoundException()
            } catch (_: TemplateLookupException.Invalid) {
                throw IllegalStateException("유효하지 않은 템플릿입니다")
            }
        repeat(MAX_CODE_GENERATION_ATTEMPTS) {
            val code = generateCode()
            if (roomAggregateRepository.findByCode(code) != null) return@repeat

            val hostId = UUID.randomUUID().toString()
            val room =
                try {
                    roomAggregateRepository.save(
                        Room.createFromTemplate(
                            code = code,
                            hostId = hostId,
                            hostNickname = hostNickname,
                            template = template,
                        ),
                    )
                } catch (_: DuplicateKeyException) {
                    return@repeat
                }

            room.recordCreated().drainEvents().forEach(events::publishEvent)
            return room
        }
        throw IllegalStateException("방 코드를 생성할 수 없습니다")
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    companion object {
        private const val MAX_CODE_GENERATION_ATTEMPTS = 5
    }
}
