package com.naminhyeok.fantazzk.room.query

import com.naminhyeok.fantazzk.room.AuctionSettled
import com.naminhyeok.fantazzk.room.RoomCompleted
import com.naminhyeok.fantazzk.room.RoomCreated
import com.naminhyeok.fantazzk.room.RoomJoined
import com.naminhyeok.fantazzk.room.RoomStarted
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
internal class RoomProjectionUpdater(
    private val roomProjectionWriter: RoomProjectionWriter,
    private val roomRepository: RoomRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
) {
    @ApplicationModuleListener
    fun on(event: RoomCreated) {
        roomProjectionWriter.upsertRoom(
            roomId = event.roomId,
            code = event.code,
            status = event.status,
        )
        roomProjectionWriter.upsertLeader(
            roomId = event.roomId,
            teamLeaderId = event.hostLeader.teamLeaderId,
            nickname = event.hostLeader.nickname,
            remainingBudget = event.hostLeader.remainingBudget,
        )
    }

    @ApplicationModuleListener
    fun on(event: RoomJoined) {
        roomProjectionWriter.upsertLeader(
            roomId = event.roomId,
            teamLeaderId = event.leader.teamLeaderId,
            nickname = event.leader.nickname,
            remainingBudget = event.leader.remainingBudget,
        )
    }

    @ApplicationModuleListener
    fun on(
        @Suppress("UNUSED_PARAMETER") event: RoomStarted,
    ) {
        refreshRoomStatus(event.roomId)
    }

    @ApplicationModuleListener
    fun on(
        @Suppress("UNUSED_PARAMETER") event: RoomCompleted,
    ) {
        refreshRoomStatus(event.roomId)
    }

    @ApplicationModuleListener
    fun on(event: AuctionSettled) {
        roomTeamLeaderRepository.findByRoomId(event.roomId).forEach { leader ->
            roomProjectionWriter.upsertLeader(
                roomId = event.roomId,
                teamLeaderId = leader.teamLeaderId,
                nickname = leader.nickname,
                remainingBudget = leader.remainingBudget,
            )
        }
    }

    private fun refreshRoomStatus(roomId: Long) {
        val room = roomRepository.findById(roomId) ?: return
        roomProjectionWriter.upsertRoom(
            roomId = room.roomId,
            code = room.code,
            status = room.status,
        )
    }
}
