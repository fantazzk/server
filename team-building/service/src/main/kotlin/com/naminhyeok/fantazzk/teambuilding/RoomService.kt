package com.naminhyeok.fantazzk.teambuilding

import com.naminhyeok.fantazzk.teambuilding.exception.RoomNotFoundException
import com.naminhyeok.fantazzk.teambuilding.repository.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.room.Player
import com.naminhyeok.fantazzk.teambuilding.room.PlayerPool
import com.naminhyeok.fantazzk.teambuilding.room.Room
import com.naminhyeok.fantazzk.teambuilding.room.RoomId
import com.naminhyeok.fantazzk.teambuilding.room.RoomSettings
import com.naminhyeok.fantazzk.teambuilding.room.TeamLeaderId
import com.naminhyeok.fantazzk.teambuilding.template.Template
import java.util.UUID

interface RoomService {
    fun create(
        template: Template,
        hostNickname: String,
    ): Room

    fun get(code: String): Room

    fun join(
        code: String,
        nickname: String,
    ): Room

    fun start(code: String): Room

    fun placeBid(
        code: String,
        teamLeaderId: TeamLeaderId,
        amount: Int,
    ): Room

    fun settleAuction(code: String): Room

    fun pick(
        code: String,
        teamLeaderId: TeamLeaderId,
        playerName: String,
    ): Room
}

internal class RoomServiceImpl(
    private val roomRepository: RoomRepository,
) : RoomService {
    override fun create(
        template: Template,
        hostNickname: String,
    ): Room {
        val settings =
            RoomSettings(
                mode = template.mode,
                rules = template.rules,
            )
        val playerPool = PlayerPool(template.players.map { Player(name = it.name, metadata = it.metadata) })
        val room =
            Room.create(
                id = RoomId(0L),
                code = generateCode(),
                hostId = TeamLeaderId(UUID.randomUUID().toString()),
                hostNickname = hostNickname,
                settings = settings,
                playerPool = playerPool,
            )
        return roomRepository.save(room)
    }

    override fun get(code: String): Room = findRoom(code)

    override fun join(
        code: String,
        nickname: String,
    ): Room {
        val room = findRoom(code)
        val updated = room.addTeamLeader(TeamLeaderId(UUID.randomUUID().toString()), nickname)
        return roomRepository.save(updated)
    }

    override fun start(code: String): Room {
        val room = findRoom(code)
        val started = room.start()
        return roomRepository.save(started)
    }

    override fun placeBid(
        code: String,
        teamLeaderId: TeamLeaderId,
        amount: Int,
    ): Room {
        val room = findRoom(code)
        val updated = room.placeBid(teamLeaderId, amount)
        return roomRepository.save(updated)
    }

    override fun settleAuction(code: String): Room {
        val room = findRoom(code)
        val updated = room.settleCurrentAuction()
        return roomRepository.save(updated)
    }

    override fun pick(
        code: String,
        teamLeaderId: TeamLeaderId,
        playerName: String,
    ): Room {
        val room = findRoom(code)
        val updated = room.pick(teamLeaderId, playerName)
        return roomRepository.save(updated)
    }

    private fun findRoom(code: String): Room = roomRepository.findByCode(code) ?: throw RoomNotFoundException()

    private fun generateCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}
