package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.outport.TemplateLookupPort
import com.naminhyeok.fantazzk.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository
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

        val room =
            roomRepository.save(
                Room(
                    code = generateUniqueCode(),
                    hostId = UUID.randomUUID().toString(),
                    status = RoomStatus.WAITING,
                    mode = template.mode,
                    teamCount = template.teamCount,
                    teamSize = template.teamSize,
                    budget = template.budget,
                    draftOrderStrategy = template.draftOrderStrategy,
                ),
            )

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

    private fun generateUniqueCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        repeat(MAX_CODE_GENERATION_ATTEMPTS) {
            val code = (1..6).map { chars.random() }.joinToString("")
            if (roomRepository.findByCode(code) == null) return code
        }
        throw IllegalStateException("방 코드를 생성할 수 없습니다")
    }

    companion object {
        private const val MAX_CODE_GENERATION_ATTEMPTS = 5
    }
}
