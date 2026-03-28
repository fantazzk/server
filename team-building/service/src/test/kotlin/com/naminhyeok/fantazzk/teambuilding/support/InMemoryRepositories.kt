package com.naminhyeok.fantazzk.teambuilding.support

import com.naminhyeok.fantazzk.teambuilding.room.PlayerStatus
import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomBid
import com.naminhyeok.fantazzk.teambuilding.room.RoomBidModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayer
import com.naminhyeok.fantazzk.teambuilding.room.RoomPlayerModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeader
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamLeaderModel
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMember
import com.naminhyeok.fantazzk.teambuilding.room.RoomTeamMemberModel
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomBidRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomTeamMemberRepository
import com.naminhyeok.fantazzk.teambuilding.template.Template
import com.naminhyeok.fantazzk.teambuilding.template.TemplateIdentity
import com.naminhyeok.fantazzk.teambuilding.template.TemplateModel
import com.naminhyeok.fantazzk.teambuilding.template.TemplatePlayer
import com.naminhyeok.fantazzk.teambuilding.template.TemplatePlayerModel
import com.naminhyeok.fantazzk.teambuilding.template.repository.TemplatePlayerRepository
import com.naminhyeok.fantazzk.teambuilding.template.repository.TemplateRepository

class InMemoryTemplateRepository : TemplateRepository {
    private val store = mutableMapOf<Long, Template>()
    private var seq = 1L

    override fun save(template: Template): TemplateModel {
        val saved = if (template.templateId == 0L) template.copy(templateId = seq++) else template
        store[saved.templateId] = saved
        return saved
    }

    override fun findById(identity: TemplateIdentity): TemplateModel? = store[identity.templateId]

    override fun findAll(): List<TemplateModel> = store.values.toList()
}

class InMemoryTemplatePlayerRepository : TemplatePlayerRepository {
    private val store = mutableListOf<TemplatePlayer>()
    private var seq = 1L

    override fun saveAll(players: List<TemplatePlayer>): List<TemplatePlayerModel> {
        val saved = players.map { if (it.templatePlayerId == 0L) it.copy(templatePlayerId = seq++) else it }
        store.addAll(saved)
        return saved
    }

    override fun findByTemplateId(templateId: Long): List<TemplatePlayerModel> = store.filter { it.templateId == templateId }
}

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
        store.add(saved)
        return saved
    }

    override fun findByRoomId(roomId: Long): List<RoomTeamMemberModel> = store.filter { it.roomId == roomId }

    override fun findByRoomIdAndTeamLeaderId(
        roomId: Long,
        teamLeaderId: String,
    ): List<RoomTeamMemberModel> = store.filter { it.roomId == roomId && it.teamLeaderId == teamLeaderId }

    override fun countByRoomId(roomId: Long): Int = store.count { it.roomId == roomId }
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
