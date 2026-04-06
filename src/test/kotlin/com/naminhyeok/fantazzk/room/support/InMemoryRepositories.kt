package com.naminhyeok.fantazzk.room.support

import com.naminhyeok.fantazzk.room.RoomId
import com.naminhyeok.fantazzk.room.domain.Room
import com.naminhyeok.fantazzk.room.repository.Rooms
import com.naminhyeok.fantazzk.template.TemplateBlueprint
import com.naminhyeok.fantazzk.template.TemplateCatalog
import com.naminhyeok.fantazzk.template.TemplateCatalogException
import com.naminhyeok.fantazzk.template.TemplateId

class InMemoryRoomRepository : Rooms {
    private val store = mutableMapOf<RoomId, Room>()

    override fun save(room: Room): Room {
        val savedRoom =
            copyRoom(
                room,
                players =
                    room.players.map {
                        playerFixture(
                            roomPlayerId = it.roomPlayerId,
                            roomId = room.roomId,
                            name = it.name,
                            status = it.status,
                            displayOrder = it.displayOrder,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt,
                        )
                    },
                leaders =
                    room.leaders.map {
                        leaderFixture(
                            roomTeamLeaderId = it.roomTeamLeaderId,
                            roomId = room.roomId,
                            teamLeaderId = it.teamLeaderId,
                            nickname = it.nickname,
                            remainingBudget = it.remainingBudget,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt,
                        )
                    },
                members =
                    room.members.map {
                        memberFixture(
                            roomTeamMemberId = it.roomTeamMemberId,
                            roomId = room.roomId,
                            teamLeaderId = it.teamLeaderId,
                            playerName = it.playerName,
                            assignOrder = it.assignOrder,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt,
                        )
                    },
                bids =
                    room.bidHistory().map {
                        bidFixture(
                            roomBidId = it.roomBidId,
                            roomId = room.roomId,
                            round = it.round,
                            teamLeaderId = it.teamLeaderId,
                            amount = it.amount,
                            createdAt = it.createdAt,
                            updatedAt = it.updatedAt,
                        )
                    },
            )

        store[savedRoom.roomId] = savedRoom.snapshot()
        return savedRoom
    }

    override fun findByCode(code: String): Room? = store.values.firstOrNull { it.code == code }?.snapshot()

    override fun findById(roomId: RoomId): Room? = store[roomId]?.snapshot()

    private fun Room.snapshot(): Room =
        copyRoom(
            this,
            players =
                players.map {
                    playerFixture(
                        roomPlayerId = it.roomPlayerId,
                        roomId = roomId,
                        name = it.name,
                        status = it.status,
                        displayOrder = it.displayOrder,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    )
                },
            leaders =
                leaders.map {
                    leaderFixture(
                        roomTeamLeaderId = it.roomTeamLeaderId,
                        roomId = roomId,
                        teamLeaderId = it.teamLeaderId,
                        nickname = it.nickname,
                        remainingBudget = it.remainingBudget,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    )
                },
            members =
                members.map {
                    memberFixture(
                        roomTeamMemberId = it.roomTeamMemberId,
                        roomId = roomId,
                        teamLeaderId = it.teamLeaderId,
                        playerName = it.playerName,
                        assignOrder = it.assignOrder,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    )
                },
            bids =
                bidHistory().map {
                    bidFixture(
                        roomBidId = it.roomBidId,
                        roomId = roomId,
                        round = it.round,
                        teamLeaderId = it.teamLeaderId,
                        amount = it.amount,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    )
                },
        )
}

class InMemoryTemplateCatalog : TemplateCatalog {
    private val templates = mutableMapOf<TemplateId, TemplateBlueprint>()

    fun addTemplate(
        templateId: TemplateId,
        snapshot: TemplateBlueprint,
    ) {
        templates[templateId] = snapshot
    }

    override fun getTemplateBlueprint(templateId: TemplateId): TemplateBlueprint =
        templates[templateId] ?: throw TemplateCatalogException.NotFound(templateId)
}
