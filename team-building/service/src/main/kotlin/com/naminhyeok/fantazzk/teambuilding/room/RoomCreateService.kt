package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.teambuilding.template.TemplateModel
import com.naminhyeok.fantazzk.teambuilding.template.TemplatePlayerModel
import java.util.UUID

interface RoomCreateService {
    fun create(
        template: TemplateModel,
        players: List<TemplatePlayerModel>,
        hostNickname: String,
    ): RoomModel
}

internal class RoomCreateServiceImpl(
    private val roomRepository: RoomRepository,
    private val roomPlayerRepository: RoomPlayerRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
) : RoomCreateService {
    override fun create(
        template: TemplateModel,
        players: List<TemplatePlayerModel>,
        hostNickname: String,
    ): RoomModel {
        val room =
            roomRepository.save(
                Room(
                    code = generateCode(),
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
            players.map { player ->
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

    private fun generateCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
