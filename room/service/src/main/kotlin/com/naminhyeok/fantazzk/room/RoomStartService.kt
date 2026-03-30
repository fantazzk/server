package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository

interface RoomStartService {
    fun start(code: String)
}

internal class RoomStartServiceImpl(
    private val roomRepository: RoomRepository,
    private val roomTeamLeaderRepository: RoomTeamLeaderRepository,
) : RoomStartService {
    override fun start(code: String) {
        val room = Room.from(roomRepository.findByCode(code) ?: throw RoomException.RoomNotFoundException())
        val leaders = roomTeamLeaderRepository.findByRoomId(room.roomId)
        roomRepository.save(room.start(leaders.size))
    }
}
