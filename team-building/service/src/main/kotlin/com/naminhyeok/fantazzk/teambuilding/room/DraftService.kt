package com.naminhyeok.fantazzk.teambuilding.room

import com.naminhyeok.fantazzk.teambuilding.DraftOrderStrategy
import com.naminhyeok.fantazzk.teambuilding.exception.RoomNotFoundException
import com.naminhyeok.fantazzk.teambuilding.exception.RoomTeamLeaderNotFoundException
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.teambuilding.room.repository.RoomTeamMemberRepository

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
        val room = roomRepository.findByCode(code) ?: throw RoomNotFoundException()
        check(room.isInProgress()) { "진행 중인 방에서만 가능합니다" }
        check(room.isDraft()) { "드래프트 모드가 아닙니다" }

        val leaders = roomTeamLeaderRepository.findByRoomId(room.roomId)
        val strategy = requireNotNull(room.draftOrderStrategy) { "드래프트 모드에는 순서 전략이 필요합니다" }
        val pickOrder = generatePickOrder(leaders.map { it.teamLeaderId }, strategy, room.picksPerTeam)
        val currentTurn = pickOrder[room.currentTurnIndex ?: 0]
        check(currentTurn == teamLeaderId) { "현재 턴이 아닙니다" }

        leaders.firstOrNull { it.teamLeaderId == teamLeaderId }
            ?: throw RoomTeamLeaderNotFoundException()

        val players = roomPlayerRepository.findByRoomId(room.roomId)
        val target = players.firstOrNull { it.name == playerName && it.status == PlayerStatus.AVAILABLE }
        requireNotNull(target) { "선수 '$playerName'은(는) 선택할 수 없습니다" }

        roomPlayerRepository.save(RoomPlayer.from(target).copy(status = PlayerStatus.ASSIGNED))

        val assignedCount = roomTeamMemberRepository.countByRoomId(room.roomId)
        val member =
            roomTeamMemberRepository.save(
                RoomTeamMember(
                    roomId = room.roomId,
                    teamLeaderId = teamLeaderId,
                    playerName = playerName,
                    assignOrder = assignedCount,
                ),
            )

        val newTurnIndex = (room.currentTurnIndex ?: 0) + 1
        val totalRequired = room.teamCount * room.picksPerTeam
        val completed = assignedCount + 1 >= totalRequired

        roomRepository.save(
            Room.from(room).copy(
                currentTurnIndex = newTurnIndex,
                status = if (completed) RoomStatus.COMPLETED else room.status,
            ),
        )

        return member
    }

    companion object {
        fun generatePickOrder(
            teamLeaderIds: List<String>,
            strategy: DraftOrderStrategy,
            picksPerTeam: Int,
        ): List<String> {
            val reversed = if (strategy == DraftOrderStrategy.SNAKE) teamLeaderIds.reversed() else null
            return (0 until picksPerTeam).flatMap { round ->
                when (strategy) {
                    DraftOrderStrategy.SNAKE -> if (round % 2 == 0) teamLeaderIds else reversed!!
                    DraftOrderStrategy.FIXED -> teamLeaderIds
                }
            }
        }
    }
}
