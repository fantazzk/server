package com.naminhyeok.fantazzk.room.service.support

import com.naminhyeok.fantazzk.room.infrastructure.RoomBidRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.infrastructure.RoomTeamMemberRepository
import com.naminhyeok.fantazzk.room.model.PlayerStatus
import com.naminhyeok.fantazzk.room.model.Room
import com.naminhyeok.fantazzk.room.model.RoomBid
import com.naminhyeok.fantazzk.room.model.RoomBidModel
import com.naminhyeok.fantazzk.room.model.RoomModel
import com.naminhyeok.fantazzk.room.model.RoomPlayer
import com.naminhyeok.fantazzk.room.model.RoomPlayerModel
import com.naminhyeok.fantazzk.room.model.RoomTeamLeader
import com.naminhyeok.fantazzk.room.model.RoomTeamLeaderModel
import com.naminhyeok.fantazzk.room.model.RoomTeamMember
import com.naminhyeok.fantazzk.room.model.RoomTeamMemberModel
import com.naminhyeok.fantazzk.template.api.TemplateLookup
import com.naminhyeok.fantazzk.template.api.TemplateView

class InMemoryRoomRepository : RoomRepository {
    private val store = mutableMapOf<Long, Room>()
    private var seq = 1L

    override fun save(room: Room): RoomModel {
        val saved = if (room.roomId == 0L) room.copy(roomId = seq++) else room
        store[saved.roomId] = saved
        return saved
    }

    override fun findByCode(code: String): RoomModel? = store.values.firstOrNull { it.code == code }

    override fun findById(roomId: Long): RoomModel? = store[roomId]
}

class InMemoryRoomPlayerRepository : RoomPlayerRepository {
    private val store = mutableListOf<RoomPlayer>()
    private var seq = 1L

    override fun save(player: RoomPlayer): RoomPlayerModel {
        val saved = if (player.roomPlayerId == 0L) player.copy(roomPlayerId = seq++) else player
        val idx = store.indexOfFirst { it.roomPlayerId == saved.roomPlayerId }
        if (idx >= 0) store[idx] = saved else store.add(saved)
        return saved
    }

    override fun saveAll(players: List<RoomPlayer>): List<RoomPlayerModel> {
        val saved = players.map { if (it.roomPlayerId == 0L) it.copy(roomPlayerId = seq++) else it }
        store.addAll(saved)
        return saved
    }

    override fun findByRoomId(roomId: Long): List<RoomPlayerModel> = store.filter { it.roomId == roomId }.sortedBy { it.displayOrder }

    override fun findFirstAvailable(roomId: Long): RoomPlayerModel? =
        store.filter { it.roomId == roomId && it.status == PlayerStatus.AVAILABLE }
            .minByOrNull { it.displayOrder }
}

class InMemoryRoomTeamLeaderRepository : RoomTeamLeaderRepository {
    private val store = mutableListOf<RoomTeamLeader>()
    private var seq = 1L

    override fun save(leader: RoomTeamLeader): RoomTeamLeaderModel {
        val saved = if (leader.roomTeamLeaderId == 0L) leader.copy(roomTeamLeaderId = seq++) else leader
        val idx = store.indexOfFirst { it.roomTeamLeaderId == saved.roomTeamLeaderId }
        if (idx >= 0) store[idx] = saved else store.add(saved)
        return saved
    }

    override fun findByRoomId(roomId: Long): List<RoomTeamLeaderModel> = store.filter { it.roomId == roomId }

    override fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): RoomTeamLeaderModel? = store.firstOrNull { it.roomId == roomId && it.teamLeaderId == teamLeaderId }
}

class InMemoryRoomTeamMemberRepository : RoomTeamMemberRepository {
    private val store = mutableListOf<RoomTeamMember>()
    private var seq = 1L

    override fun save(member: RoomTeamMember): RoomTeamMemberModel {
        val saved = if (member.roomTeamMemberId == 0L) member.copy(roomTeamMemberId = seq++) else member
        val idx = store.indexOfFirst { it.roomTeamMemberId == saved.roomTeamMemberId }
        if (idx >= 0) store[idx] = saved else store.add(saved)
        return saved
    }

    override fun findByRoomId(roomId: Long): List<RoomTeamMemberModel> = store.filter { it.roomId == roomId }

    override fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): List<RoomTeamMemberModel> = store.filter { it.roomId == roomId && it.teamLeaderId == teamLeaderId }

    override fun countByRoomId(roomId: Long): Int = store.count { it.roomId == roomId }

    override fun countByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): Int = store.count { it.roomId == roomId && it.teamLeaderId == teamLeaderId }
}

class InMemoryRoomBidRepository : RoomBidRepository {
    private val store = mutableListOf<RoomBid>()
    private var seq = 1L

    override fun save(bid: RoomBid): RoomBidModel {
        val saved = if (bid.roomBidId == 0L) bid.copy(roomBidId = seq++) else bid
        store.add(saved)
        return saved
    }

    override fun findByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): List<RoomBidModel> = store.filter { it.roomId == roomId && it.round == round }

    override fun findHighestByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): RoomBidModel? = store.filter { it.roomId == roomId && it.round == round }.maxByOrNull { it.amount }
}

class InMemoryTemplateLookup : TemplateLookup {
    private val templates = mutableMapOf<Long, TemplateView>()

    fun addTemplate(
        templateId: Long,
        snapshot: TemplateView,
    ) {
        templates[templateId] = snapshot
    }

    override fun get(templateId: Long): TemplateView =
        templates[templateId] ?: throw IllegalArgumentException("Template not found: $templateId")
}
