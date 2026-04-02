package com.naminhyeok.fantazzk.room.support

import com.naminhyeok.fantazzk.room.PlayerStatus
import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomBid
import com.naminhyeok.fantazzk.room.RoomPlayer
import com.naminhyeok.fantazzk.room.RoomTeamLeader
import com.naminhyeok.fantazzk.room.RoomTeamMember
import com.naminhyeok.fantazzk.room.repository.RoomBidRepository
import com.naminhyeok.fantazzk.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamMemberRepository
import com.naminhyeok.fantazzk.template.spi.TemplateLookup
import com.naminhyeok.fantazzk.template.spi.TemplateSnapshot

class InMemoryRoomRepository : RoomRepository {
    private val store = mutableMapOf<Long, Room>()
    private var seq = 1L

    override fun save(room: Room): Room {
        val saved = if (room.roomId == 0L) room.copy(roomId = seq++) else room
        store[saved.roomId] = saved
        return saved
    }

    override fun findByCode(code: String): Room? = store.values.firstOrNull { it.code == code }

    override fun findById(roomId: Long): Room? = store[roomId]
}

class InMemoryRoomPlayerRepository : RoomPlayerRepository {
    private val store = mutableListOf<RoomPlayer>()
    private var seq = 1L

    override fun save(player: RoomPlayer): RoomPlayer {
        val saved = if (player.roomPlayerId == 0L) player.copy(roomPlayerId = seq++) else player
        val idx = store.indexOfFirst { it.roomPlayerId == saved.roomPlayerId }
        if (idx >= 0) store[idx] = saved else store.add(saved)
        return saved
    }

    override fun saveAll(players: List<RoomPlayer>): List<RoomPlayer> {
        val saved = players.map { if (it.roomPlayerId == 0L) it.copy(roomPlayerId = seq++) else it }
        store.addAll(saved)
        return saved
    }

    override fun findByRoomId(roomId: Long): List<RoomPlayer> = store.filter { it.roomId == roomId }.sortedBy { it.displayOrder }

    override fun findFirstAvailable(roomId: Long): RoomPlayer? =
        store.filter { it.roomId == roomId && it.status == PlayerStatus.AVAILABLE }
            .minByOrNull { it.displayOrder }
}

class InMemoryRoomTeamLeaderRepository : RoomTeamLeaderRepository {
    private val store = mutableListOf<RoomTeamLeader>()
    private var seq = 1L

    override fun save(leader: RoomTeamLeader): RoomTeamLeader {
        val saved = if (leader.roomTeamLeaderId == 0L) leader.copy(roomTeamLeaderId = seq++) else leader
        val idx = store.indexOfFirst { it.roomTeamLeaderId == saved.roomTeamLeaderId }
        if (idx >= 0) store[idx] = saved else store.add(saved)
        return saved
    }

    override fun findByRoomId(roomId: Long): List<RoomTeamLeader> = store.filter { it.roomId == roomId }

    override fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): RoomTeamLeader? = store.firstOrNull { it.roomId == roomId && it.teamLeaderId == teamLeaderId }
}

class InMemoryRoomTeamMemberRepository : RoomTeamMemberRepository {
    private val store = mutableListOf<RoomTeamMember>()
    private var seq = 1L

    override fun save(member: RoomTeamMember): RoomTeamMember {
        val saved = if (member.roomTeamMemberId == 0L) member.copy(roomTeamMemberId = seq++) else member
        val idx = store.indexOfFirst { it.roomTeamMemberId == saved.roomTeamMemberId }
        if (idx >= 0) store[idx] = saved else store.add(saved)
        return saved
    }

    override fun findByRoomId(roomId: Long): List<RoomTeamMember> = store.filter { it.roomId == roomId }

    override fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): List<RoomTeamMember> = store.filter { it.roomId == roomId && it.teamLeaderId == teamLeaderId }

    override fun countByRoomId(roomId: Long): Int = store.count { it.roomId == roomId }

    override fun countByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): Int = store.count { it.roomId == roomId && it.teamLeaderId == teamLeaderId }
}

class InMemoryRoomBidRepository : RoomBidRepository {
    private val store = mutableListOf<RoomBid>()
    private var seq = 1L

    override fun save(bid: RoomBid): RoomBid {
        val saved = if (bid.roomBidId == 0L) bid.copy(roomBidId = seq++) else bid
        store.add(saved)
        return saved
    }

    override fun findByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): List<RoomBid> = store.filter { it.roomId == roomId && it.round == round }

    override fun findHighestByRoomIdAndRound(
        roomId: Long,
        round: Int,
    ): RoomBid? = store.filter { it.roomId == roomId && it.round == round }.maxByOrNull { it.amount }
}

class InMemoryTemplateLookup : TemplateLookup {
    private val templates = mutableMapOf<Long, TemplateSnapshot>()

    fun addTemplate(
        templateId: Long,
        snapshot: TemplateSnapshot,
    ) {
        templates[templateId] = snapshot
    }

    override fun getTemplate(templateId: Long): TemplateSnapshot =
        templates[templateId] ?: throw com.naminhyeok.fantazzk.template.spi.TemplateLookupException.NotFound(templateId)
}
