package com.naminhyeok.fantazzk.room.application

import com.naminhyeok.fantazzk.room.domain.Room
import com.naminhyeok.fantazzk.room.exception.RoomTemplateNotFoundException
import com.naminhyeok.fantazzk.room.repository.Rooms
import com.naminhyeok.fantazzk.template.TemplateBlueprint
import com.naminhyeok.fantazzk.template.TemplateCatalog
import com.naminhyeok.fantazzk.template.TemplateCatalogException
import com.naminhyeok.fantazzk.template.TemplateDraftOrderStrategy
import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.TemplateMode
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CreateRoom(
    private val roomRepository: Rooms,
    private val templateCatalog: TemplateCatalog,
    private val roomCreateAttemptExecutor: RoomCreateAttemptExecutor,
) {
    fun create(
        templateId: TemplateId,
        hostNickname: String,
    ): Room {
        val template =
            try {
                templateCatalog.get(templateId).toRoomTemplateSpec()
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
                    roomCreateAttemptExecutor.create(
                        Room.createFromTemplate(
                            code = code,
                            hostId = hostId,
                            hostNickname = hostNickname,
                            spec = template,
                        ),
                    )
                } catch (_: DuplicateKeyException) {
                    return@repeat
                } catch (exception: DataIntegrityViolationException) {
                    if (roomRepository.findByCode(code) != null) {
                        return@repeat
                    }
                    throw exception
                }
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

private fun TemplateBlueprint.toRoomTemplateSpec(): RoomTemplateSpec =
    RoomTemplateSpec(
        mode =
            when (mode) {
                TemplateMode.AUCTION -> RoomTemplateSpec.Mode.AUCTION
                TemplateMode.DRAFT -> RoomTemplateSpec.Mode.DRAFT
            },
        teamCount = teamCount,
        teamSize = teamSize,
        budget = budget,
        draftOrderStrategy =
            when (draftOrderStrategy) {
                null -> null
                TemplateDraftOrderStrategy.FIXED -> RoomTemplateSpec.DraftOrderStrategy.FIXED
                TemplateDraftOrderStrategy.SNAKE -> RoomTemplateSpec.DraftOrderStrategy.SNAKE
            },
        players =
            players.map {
                RoomTemplateSpec.Player(
                    name = it.name,
                    displayOrder = it.displayOrder,
                )
            },
    )
