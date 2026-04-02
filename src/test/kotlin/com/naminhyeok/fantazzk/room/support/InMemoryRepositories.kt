package com.naminhyeok.fantazzk.room.support

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomId
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.template.TemplateBlueprint
import com.naminhyeok.fantazzk.template.TemplateCatalog
import com.naminhyeok.fantazzk.template.TemplateCatalogException

class InMemoryRoomRepository : RoomRepository {
    private val store = mutableMapOf<Long, Room>()
    private var roomSeq = 1L
    private var playerSeq = 1L
    private var leaderSeq = 1L
    private var memberSeq = 1L
    private var bidSeq = 1L

    override fun save(room: Room): Room {
        val pendingEvents = room.pendingEvents()
        val assignedRoomId = if (room.roomId == 0L) roomSeq++ else room.roomId
        val savedRoom =
            room.copy(
                roomId = assignedRoomId,
                players =
                    room.players.map {
                        it.copy(
                            roomPlayerId = if (it.roomPlayerId == 0L) playerSeq++ else it.roomPlayerId,
                            roomId = assignedRoomId,
                        )
                    },
                leaders =
                    room.leaders.map {
                        it.copy(
                            roomTeamLeaderId = if (it.roomTeamLeaderId == 0L) leaderSeq++ else it.roomTeamLeaderId,
                            roomId = assignedRoomId,
                        )
                    },
                members =
                    room.members.map {
                        it.copy(
                            roomTeamMemberId = if (it.roomTeamMemberId == 0L) memberSeq++ else it.roomTeamMemberId,
                            roomId = assignedRoomId,
                        )
                    },
                bids =
                    room.bidHistory().map {
                        it.copy(
                            roomBidId = if (it.roomBidId == 0L) bidSeq++ else it.roomBidId,
                            roomId = assignedRoomId,
                        )
                    },
            )

        store[assignedRoomId] = savedRoom.snapshot()
        return savedRoom.restorePendingEvents(pendingEvents)
    }

    override fun findByCode(code: String): Room? = store.values.firstOrNull { it.code == code }?.snapshot()

    override fun findById(roomId: RoomId): Room? = store[roomId.value]?.snapshot()

    private fun Room.snapshot(): Room =
        Room(
            roomId = roomId,
            code = code,
            hostId = hostId,
            status = status,
            mode = mode,
            teamCount = teamCount,
            teamSize = teamSize,
            budget = budget,
            draftOrderStrategy = draftOrderStrategy,
            currentTurnIndex = currentTurnIndex,
            currentAuctionRound = currentAuctionRound,
            players = players.map { it.copy() },
            leaders = leaders.map { it.copy() },
            members = members.map { it.copy() },
            bids = bidHistory().map { it.copy() },
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}

class InMemoryTemplateCatalog : TemplateCatalog {
    private val templates = mutableMapOf<Long, TemplateBlueprint>()

    fun addTemplate(
        templateId: Long,
        snapshot: TemplateBlueprint,
    ) {
        templates[templateId] = snapshot
    }

    override fun getTemplateBlueprint(templateId: Long): TemplateBlueprint =
        templates[templateId] ?: throw TemplateCatalogException.NotFound(templateId)
}
