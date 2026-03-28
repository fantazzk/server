package com.naminhyeok.fantazzk.teambuilding.room

interface RoomPlayerIdentity {
    val roomPlayerId: Long
}

interface RoomPlayerProps {
    val roomId: Long
    val name: String
    val status: PlayerStatus
    val displayOrder: Int
}

interface RoomPlayerModel : RoomPlayerIdentity, RoomPlayerProps
