package com.naminhyeok.fantazzk.room

sealed interface RoomProgress {
    val status: RoomStatus

    data object Waiting : RoomProgress {
        override val status: RoomStatus = RoomStatus.WAITING
    }

    data class Auction(
        val currentRound: Int,
    ) : RoomProgress {
        override val status: RoomStatus = RoomStatus.IN_PROGRESS
    }

    data class Draft(
        val currentTurnIndex: Int,
    ) : RoomProgress {
        override val status: RoomStatus = RoomStatus.IN_PROGRESS
    }

    data object Completed : RoomProgress {
        override val status: RoomStatus = RoomStatus.COMPLETED
    }

    companion object {
        fun from(room: Room): RoomProgress =
            when (room.status) {
                RoomStatus.WAITING -> Waiting
                RoomStatus.COMPLETED -> Completed
                RoomStatus.IN_PROGRESS ->
                    when (room.mode) {
                        TeamBuildingMode.AUCTION ->
                            Auction(
                                currentRound =
                                    requireNotNull(room.currentAuctionRound) {
                                        "진행 중인 경매 방에서는 현재 라운드가 존재해야 합니다"
                                    },
                            )

                        TeamBuildingMode.DRAFT ->
                            Draft(
                                currentTurnIndex =
                                    requireNotNull(room.currentTurnIndex) {
                                        "진행 중인 드래프트 방에서는 현재 턴이 존재해야 합니다"
                                    },
                            )
                    }
            }

    }
}
