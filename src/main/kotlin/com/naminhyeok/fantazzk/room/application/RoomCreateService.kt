package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.exception.RoomTemplateNotFoundException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.template.TemplateCatalog
import com.naminhyeok.fantazzk.template.TemplateCatalogException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface RoomCreateService {
    fun create(
        templateId: Long,
        hostNickname: String,
    ): Room
}

@org.jmolecules.ddd.annotation.Service
@Service
internal class RoomCreateServiceImpl(
    private val roomRepository: RoomRepository,
    private val templateCatalog: TemplateCatalog,
    private val events: ApplicationEventPublisher,
) : RoomCreateService {
    @Transactional
    override fun create(
        templateId: Long,
        hostNickname: String,
    ): Room {
        val template =
            try {
                templateCatalog.getTemplateBlueprint(templateId)
            } catch (_: TemplateCatalogException.NotFound) {
                throw RoomTemplateNotFoundException()
            } catch (_: TemplateCatalogException.Invalid) {
                throw IllegalStateException("유효하지 않은 템플릿입니다")
            }
        repeat(MAX_CODE_GENERATION_ATTEMPTS) {
            val code = generateCode()
            if (roomRepository.findByCode(code) != null) return@repeat

            val hostId = UUID.randomUUID().toString()
            val room =
                try {
                    roomRepository.save(
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
