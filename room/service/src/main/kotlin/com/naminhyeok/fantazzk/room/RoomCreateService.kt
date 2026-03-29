package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.exception.RoomTemplateNotFoundException
import com.naminhyeok.fantazzk.room.outport.TemplateLookupPort
import com.naminhyeok.fantazzk.room.outport.TemplateLookupPortException
import com.naminhyeok.fantazzk.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository
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
    private val templateLookupPort: TemplateLookupPort,
) : RoomCreateService {
    override fun create(
        templateId: Long,
        hostNickname: String,
    ): RoomModel {
        val template =
            try {
                templateLookupPort.getTemplate(templateId)
            } catch (_: TemplateLookupPortException.NotFound) {
                throw RoomTemplateNotFoundException()
            }
        repeat(MAX_CODE_GENERATION_ATTEMPTS) {
            val code = generateCode()
            if (roomRepository.findByCode(code) != null) return@repeat

            val hostId = UUID.randomUUID().toString()
            val room =
                try {
                    roomRepository.save(
                        when (template.mode) {
                            TeamBuildingMode.AUCTION ->
                                Room.createAuction(
                                    code = code,
                                    hostId = hostId,
                                    teamCount = template.teamCount,
                                    teamSize = template.teamSize,
                                    budget = requireNotNull(template.budget) { "경매 템플릿에는 예산이 필요합니다" },
                                )

                            TeamBuildingMode.DRAFT ->
                                Room.createDraft(
                                    code = code,
                                    hostId = hostId,
                                    teamCount = template.teamCount,
                                    teamSize = template.teamSize,
                                    draftOrderStrategy =
                                        requireNotNull(template.draftOrderStrategy) {
                                            "드래프트 템플릿에는 순서 전략이 필요합니다"
                                        },
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
