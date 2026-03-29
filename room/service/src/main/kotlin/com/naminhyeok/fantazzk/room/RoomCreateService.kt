package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.outport.TemplateFetcher
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
    private val templateFetcher: TemplateFetcher,
) : RoomCreateService {
    override fun create(
        templateId: Long,
        hostNickname: String,
    ): RoomModel {
        val template = templateFetcher.getTemplate(templateId)
        val players = templateFetcher.getPlayers(templateId)

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
