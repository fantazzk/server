package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.outport.TemplateLookupPort
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
        val template = templateLookupPort.getTemplate(templateId)
        repeat(MAX_CODE_GENERATION_ATTEMPTS) {
            val code = generateCode()
            if (roomRepository.findByCode(code) != null) return@repeat

            val room =
                try {
                    roomRepository.save(
                        Room(
                            code = code,
                            hostId = UUID.randomUUID().toString(),
                            status = RoomStatus.WAITING,
                            mode = template.mode,
                            teamCount = template.teamCount,
                            teamSize = template.teamSize,
                            budget = template.budget,
                            draftOrderStrategy = template.draftOrderStrategy,
                        ),
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
                RoomTeamLeader(
                    roomId = room.roomId,
                    teamLeaderId = room.hostId,
                    nickname = hostNickname,
                    remainingBudget = template.budget,
                ),
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
