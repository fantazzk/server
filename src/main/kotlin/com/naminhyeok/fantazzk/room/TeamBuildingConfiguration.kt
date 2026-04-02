package com.naminhyeok.fantazzk.room

sealed interface TeamBuildingConfiguration {
    val mode: TeamBuildingMode
    val teamCount: Int
    val teamSize: Int

    data class Auction(
        override val teamCount: Int,
        override val teamSize: Int,
        val budget: Int,
    ) : TeamBuildingConfiguration {
        override val mode: TeamBuildingMode = TeamBuildingMode.AUCTION
    }

    data class Draft(
        override val teamCount: Int,
        override val teamSize: Int,
        val strategy: DraftOrderStrategy,
    ) : TeamBuildingConfiguration {
        override val mode: TeamBuildingMode = TeamBuildingMode.DRAFT
    }

    companion object {
        fun from(room: Room): TeamBuildingConfiguration =
            when (room.mode) {
                TeamBuildingMode.AUCTION ->
                    Auction(
                        teamCount = room.teamCount,
                        teamSize = room.teamSize,
                        budget = requireNotNull(room.budget) { "경매 모드에서는 예산이 존재해야 합니다" },
                    )

                TeamBuildingMode.DRAFT ->
                    Draft(
                        teamCount = room.teamCount,
                        teamSize = room.teamSize,
                        strategy =
                            requireNotNull(room.draftOrderStrategy) { "드래프트 모드에서는 순서 전략이 존재해야 합니다" },
                    )
            }

        fun from(room: RoomProps): TeamBuildingConfiguration =
            when (room.mode) {
                TeamBuildingMode.AUCTION ->
                    Auction(
                        teamCount = room.teamCount,
                        teamSize = room.teamSize,
                        budget = requireNotNull(room.budget) { "경매 모드에서는 예산이 존재해야 합니다" },
                    )

                TeamBuildingMode.DRAFT ->
                    Draft(
                        teamCount = room.teamCount,
                        teamSize = room.teamSize,
                        strategy =
                            requireNotNull(room.draftOrderStrategy) { "드래프트 모드에서는 순서 전략이 존재해야 합니다" },
                    )
            }
    }
}
