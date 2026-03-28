package com.naminhyeok.fantazzk.teambuilding.room

interface RoomPlayerProps {
    val roomId: Long
    val name: String
    val status: PlayerStatus
    val displayOrder: Int
}
