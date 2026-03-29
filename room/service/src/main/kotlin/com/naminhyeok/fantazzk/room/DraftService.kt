package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamMemberRepository

interface DraftService {
    fun pick(
        code: String,
        teamLeaderId: String,
        playerName: String,
    ): RoomTeamMemberModel
}

internal class DraftServiceImpl(
    private val roomRepository: RoomRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
    private val roomPlayerRepository: RoomPlayerRepository,
    private val roomTeamMemberRepository: RoomTeamMemberRepository,
) : DraftService {
    override fun pick(
        code: String,
        teamLeaderId: String,
        playerName: String,
    ): RoomTeamMemberModel {
        val room = roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException()
        check(room.isInProgress()) { "진행 중인 방에서만 가능합니다" }
        check(room.isDraft()) { "드래프트 모드가 아닙니다" }
        val turnIndex = room.requireCurrentTurnIndex()

        val leaders = roomTeamLeaderRepository.findByRoomId(room.roomId)
        val strategy = requireNotNull(room.draftOrderStrategy) { "드래프트 모드에는 순서 전략이 필요합니다" }
        val draftBoard =
            DraftBoard(
                teamLeaderIds = leaders.map { it.teamLeaderId },
                strategy = strategy,
                picksPerTeam = room.picksPerTeam,
            )
        draftBoard.requireTurnOwner(turnIndex = turnIndex, teamLeaderId = teamLeaderId)

        leaders.firstOrNull { it.teamLeaderId == teamLeaderId }
            ?: throw RoomException.TeamLeaderNotFoundException()

        val players = roomPlayerRepository.findByRoomId(room.roomId)
        val target = players.firstOrNull { it.name == playerName && it.status == PlayerStatus.AVAILABLE }
        requireNotNull(target) { "선수 '$playerName'은(는) 선택할 수 없습니다" }

        val assignedCount = roomTeamMemberRepository.countByRoomId(room.roomId)
        val settlement = draftBoard.settlePick(turnIndex = turnIndex, assignedCountAfterPick = assignedCount + 1)
        val nextRoom = room.advanceDraftTurn(nextTurnIndex = settlement.nextTurnIndex, completed = settlement.completed)
        val assignedPlayer = target.assign()
        val member =
            RoomTeamMember(
                roomId = room.roomId,
                teamLeaderId = teamLeaderId,
                playerName = playerName,
                assignOrder = assignedCount,
            )

        roomPlayerRepository.save(assignedPlayer)
        val savedMember = roomTeamMemberRepository.save(member)
        roomRepository.save(nextRoom)

        return savedMember
    }
}
