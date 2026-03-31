package com.naminhyeok.fantazzk.room.service

import com.naminhyeok.fantazzk.room.exception.RoomTemplateNotFoundException
import com.naminhyeok.fantazzk.room.infrastructure.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.model.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.model.Room
import com.naminhyeok.fantazzk.room.model.RoomModel
import com.naminhyeok.fantazzk.room.model.RoomPlayer
import com.naminhyeok.fantazzk.template.api.TemplateDraftStrategy
import com.naminhyeok.fantazzk.template.api.TemplateLookup
import com.naminhyeok.fantazzk.template.api.TemplateLookupException
import com.naminhyeok.fantazzk.template.api.TemplateMode
import org.springframework.dao.DuplicateKeyException
import java.util.UUID

interface RoomCreateService {
    fun create(
        templateId: Long,
        hostNickname: String,
    ): RoomModel
}

internal class RoomCreateServiceImpl(
    private val roomRepository: RoomRepository,
    private val roomPlayerRepository: RoomPlayerRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
    private val templateLookup: TemplateLookup,
) : RoomCreateService {
    override fun create(
        templateId: Long,
        hostNickname: String,
    ): RoomModel {
        val template =
            try {
                templateLookup.get(templateId)
            } catch (_: TemplateLookupException.NotFound) {
                throw RoomTemplateNotFoundException()
            } catch (_: TemplateLookupException.Invalid) {
                throw IllegalStateException("유효하지 않은 템플릿입니다")
            }
        repeat(MAX_CODE_GENERATION_ATTEMPTS) {
            val code = generateCode()
            if (roomRepository.findByCode(code) != null) return@repeat

            val hostId = UUID.randomUUID().toString()
            val room =
                try {
                    roomRepository.save(
                        when (template.mode) {
                            TemplateMode.AUCTION ->
                                Room.createAuction(
                                    code = code,
                                    hostId = hostId,
                                    teamCount = template.teamCount,
                                    teamSize = template.teamSize,
                                    budget = requireNotNull(template.budget) { "경매 템플릿에는 예산이 필요합니다" },
                                )

                            TemplateMode.DRAFT ->
                                Room.createDraft(
                                    code = code,
                                    hostId = hostId,
                                    teamCount = template.teamCount,
                                    teamSize = template.teamSize,
                                    draftOrderStrategy =
                                        requireNotNull(template.draftOrderStrategy) {
                                            "드래프트 템플릿에는 순서 전략이 필요합니다"
                                        }.toRoomDraftOrderStrategy(),
                                )
                        },
                    )
                } catch (_: DuplicateKeyException) {
                    return@repeat
                }

            roomPlayerRepository.saveAll(
                template.players.map { player ->
                    RoomPlayer(roomId = room.roomId, name = player.name, displayOrder = player.displayOrder)
                },
            )

            roomTeamLeaderRepository.save(
                Room.from(room).createHostLeader(hostNickname),
            )

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

private fun TemplateDraftStrategy.toRoomDraftOrderStrategy(): DraftOrderStrategy = DraftOrderStrategy.valueOf(name)
