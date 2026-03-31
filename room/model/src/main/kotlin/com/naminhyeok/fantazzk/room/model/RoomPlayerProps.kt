package com.naminhyeok.fantazzk.room.model

interface RoomPlayerProps {
    val roomId: Long
    val name: String
    val status: PlayerStatus
    val displayOrder: Int
}
