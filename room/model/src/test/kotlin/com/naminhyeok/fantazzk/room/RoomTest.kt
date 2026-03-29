package com.naminhyeok.fantazzk.room

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant

class RoomTest {
    @Nested
    inner class `생성 계약` {
        @Test
        fun `새 방은 기본 식별자와 선택 속성 기본값을 가진다`() {
            val beforeCreate = Instant.now()
            val room =
                Room(
                    code = "ROOM00",
                    hostId = "host-0",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 3,
                    budget = 300,
                )
            val afterCreate = Instant.now()

            assertThat(room.roomId).isZero()
            assertThat(room.budget).isEqualTo(300)
            assertThat(room.draftOrderStrategy).isNull()
            assertThat(room.currentTurnIndex).isNull()
            assertThat(room.currentAuctionRound).isNull()
            assertThat(room.createdAt).isBetween(beforeCreate, afterCreate)
            assertThat(room.updatedAt).isBetween(beforeCreate, afterCreate)
        }

        @Test
        fun `방은 선언된 속성을 그대로 노출한다`() {
            val createdAt = Instant.parse("2025-01-01T00:00:00Z")
            val updatedAt = Instant.parse("2025-01-02T00:00:00Z")

            val room =
                room(
                    roomId = 10L,
                    code = "ROOM10",
                    hostId = "host-10",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.DRAFT,
                    teamCount = 4,
                    teamSize = 5,
                    budget = null,
                    draftOrderStrategy = DraftOrderStrategy.FIXED,
                    currentTurnIndex = 2,
                    currentAuctionRound = null,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                )

            assertThat(room.roomId).isEqualTo(10L)
            assertThat(room.code).isEqualTo("ROOM10")
            assertThat(room.hostId).isEqualTo("host-10")
            assertThat(room.status).isEqualTo(RoomStatus.IN_PROGRESS)
            assertThat(room.mode).isEqualTo(TeamBuildingMode.DRAFT)
            assertThat(room.teamCount).isEqualTo(4)
            assertThat(room.teamSize).isEqualTo(5)
            assertThat(room.budget).isNull()
            assertThat(room.draftOrderStrategy).isEqualTo(DraftOrderStrategy.FIXED)
            assertThat(room.currentTurnIndex).isEqualTo(2)
            assertThat(room.currentAuctionRound).isNull()
            assertThat(room.createdAt).isEqualTo(createdAt)
            assertThat(room.updatedAt).isEqualTo(updatedAt)
        }

        @Test
        fun `경매 방은 예산이 있어야 하고 드래프트 전략은 가질 수 없다`() {
            assertThatThrownBy {
                Room(
                    code = "ROOM01",
                    hostId = "host-1",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 3,
                    budget = null,
                )
            }.isInstanceOf(IllegalArgumentException::class.java)

            assertThatThrownBy {
                Room(
                    code = "ROOM02",
                    hostId = "host-2",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 3,
                    budget = 300,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `드래프트 방은 전략이 있어야 하고 예산은 가질 수 없다`() {
            assertThatThrownBy {
                Room(
                    code = "ROOM03",
                    hostId = "host-3",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.DRAFT,
                    teamCount = 2,
                    teamSize = 3,
                    draftOrderStrategy = null,
                )
            }.isInstanceOf(IllegalArgumentException::class.java)

            assertThatThrownBy {
                Room(
                    code = "ROOM04",
                    hostId = "host-4",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.DRAFT,
                    teamCount = 2,
                    teamSize = 3,
                    budget = 300,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Nested
    inner class `모드별 생성` {
        @Test
        fun `경매 방은 예산과 함께 생성된다`() {
            val room = room(mode = TeamBuildingMode.AUCTION, budget = 300)

            assertThat(room.isAuction()).isTrue()
            assertThat(room.isDraft()).isFalse()
            assertThat(room.budget).isEqualTo(300)
        }

        @Test
        fun `드래프트 방은 순서 전략과 함께 생성된다`() {
            val room = room(mode = TeamBuildingMode.DRAFT, draftOrderStrategy = DraftOrderStrategy.SNAKE)

            assertThat(room.isDraft()).isTrue()
            assertThat(room.isAuction()).isFalse()
            assertThat(room.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
        }
    }

    @Nested
    inner class `상태 판별` {
        @ParameterizedTest(name = "{0} 상태에서 isWaiting={1}")
        @EnumSource(RoomStatus::class)
        fun `isWaiting은 WAITING 상태에서만 true를 반환한다`(status: RoomStatus) {
            val room = room(status = status)
            assertThat(room.isWaiting()).isEqualTo(status == RoomStatus.WAITING)
        }

        @ParameterizedTest(name = "{0} 상태에서 isInProgress={1}")
        @EnumSource(RoomStatus::class)
        fun `isInProgress는 IN_PROGRESS 상태에서만 true를 반환한다`(status: RoomStatus) {
            val room = room(status = status)
            assertThat(room.isInProgress()).isEqualTo(status == RoomStatus.IN_PROGRESS)
        }
    }

    @Nested
    inner class `파생 값` {
        @Test
        fun `경매 방은 configuration으로 경매 설정을 노출한다`() {
            val room = room(mode = TeamBuildingMode.AUCTION, budget = 300)

            assertThat(room.configuration).isEqualTo(
                TeamBuildingConfiguration.Auction(
                    teamCount = 2,
                    teamSize = 3,
                    budget = 300,
                ),
            )
        }

        @Test
        fun `드래프트 방은 configuration으로 드래프트 설정을 노출한다`() {
            val room = room(mode = TeamBuildingMode.DRAFT, draftOrderStrategy = DraftOrderStrategy.SNAKE)

            assertThat(room.configuration).isEqualTo(
                TeamBuildingConfiguration.Draft(
                    teamCount = 2,
                    teamSize = 3,
                    strategy = DraftOrderStrategy.SNAKE,
                ),
            )
        }

        @Test
        fun `대기 방은 progress로 대기 상태를 노출한다`() {
            assertThat(room(status = RoomStatus.WAITING).progress).isEqualTo(RoomProgress.Waiting)
        }

        @Test
        fun `진행 중인 경매 방은 progress로 현재 라운드를 노출한다`() {
            val room =
                room(
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.AUCTION,
                    budget = 300,
                    currentAuctionRound = 2,
                )

            assertThat(room.progress).isEqualTo(RoomProgress.Auction(currentRound = 2))
        }

        @Test
        fun `완료된 방은 progress로 완료 상태를 노출한다`() {
            assertThat(room(status = RoomStatus.COMPLETED).progress).isEqualTo(RoomProgress.Completed)
        }

        @Test
        fun `picksPerTeam은 teamSize에서 1을 뺀 값이다`() {
            assertThat(room(teamSize = 3).picksPerTeam).isEqualTo(2)
            assertThat(room(teamSize = 5).picksPerTeam).isEqualTo(4)
            assertThat(room(teamSize = 1).picksPerTeam).isEqualTo(0)
        }
    }

    @Nested
    inner class Identity {
        @Test
        fun `RoomIdentity를 생성할 수 있다`() {
            val identity = RoomIdentity.of(42L)
            assertThat(identity.roomId).isEqualTo(42L)
        }
    }

    @Nested
    inner class `모델 변환` {
        @Test
        fun `RoomModel에서 Room을 복원할 수 있다`() {
            val createdAt = Instant.parse("2025-02-01T00:00:00Z")
            val updatedAt = Instant.parse("2025-02-02T00:00:00Z")
            val model =
                roomModel(
                    roomId = 7L,
                    code = "ROOM07",
                    hostId = "host-7",
                    status = RoomStatus.COMPLETED,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 3,
                    teamSize = 4,
                    budget = 250,
                    draftOrderStrategy = null,
                    currentTurnIndex = 1,
                    currentAuctionRound = 5,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                )

            val room = Room.from(model)

            assertThat(room).isEqualTo(
                Room(
                    roomId = 7L,
                    code = "ROOM07",
                    hostId = "host-7",
                    status = RoomStatus.COMPLETED,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 3,
                    teamSize = 4,
                    budget = 250,
                    draftOrderStrategy = null,
                    currentTurnIndex = 1,
                    currentAuctionRound = 5,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                ),
            )
        }

        @Test
        fun `legacy 드래프트 row를 복원할 때 stale budget을 제거한다`() {
            val room =
                Room.from(
                    roomModel(
                        roomId = 8L,
                        code = "ROOM08",
                        hostId = "host-8",
                        status = RoomStatus.WAITING,
                        mode = TeamBuildingMode.DRAFT,
                        teamCount = 2,
                        teamSize = 3,
                        budget = 300,
                        draftOrderStrategy = DraftOrderStrategy.SNAKE,
                        currentTurnIndex = null,
                        currentAuctionRound = null,
                        createdAt = Instant.parse("2025-02-03T00:00:00Z"),
                        updatedAt = Instant.parse("2025-02-04T00:00:00Z"),
                    ),
                )

            assertThat(room.budget).isNull()
            assertThat(room.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
            assertThat(room.configuration).isEqualTo(
                TeamBuildingConfiguration.Draft(
                    teamCount = 2,
                    teamSize = 3,
                    strategy = DraftOrderStrategy.SNAKE,
                ),
            )
        }

        @Test
        fun `legacy 경매 row를 복원할 때 stale draft strategy를 제거한다`() {
            val room =
                Room.from(
                    roomModel(
                        roomId = 9L,
                        code = "ROOM09",
                        hostId = "host-9",
                        status = RoomStatus.IN_PROGRESS,
                        mode = TeamBuildingMode.AUCTION,
                        teamCount = 2,
                        teamSize = 3,
                        budget = 300,
                        draftOrderStrategy = DraftOrderStrategy.FIXED,
                        currentTurnIndex = null,
                        currentAuctionRound = 2,
                        createdAt = Instant.parse("2025-02-05T00:00:00Z"),
                        updatedAt = Instant.parse("2025-02-06T00:00:00Z"),
                    ),
                )

            assertThat(room.budget).isEqualTo(300)
            assertThat(room.draftOrderStrategy).isNull()
            assertThat(room.progress).isEqualTo(RoomProgress.Auction(currentRound = 2))
            assertThat(room.configuration).isEqualTo(
                TeamBuildingConfiguration.Auction(
                    teamCount = 2,
                    teamSize = 3,
                    budget = 300,
                ),
            )
        }
    }

    private fun room(
        roomId: Long = 0L,
        code: String = "TEST01",
        hostId: String = "host",
        status: RoomStatus = RoomStatus.WAITING,
        mode: TeamBuildingMode = TeamBuildingMode.AUCTION,
        teamCount: Int = 2,
        teamSize: Int = 3,
        budget: Int? = if (mode == TeamBuildingMode.AUCTION) 300 else null,
        draftOrderStrategy: DraftOrderStrategy? = if (mode == TeamBuildingMode.DRAFT) DraftOrderStrategy.SNAKE else null,
        currentTurnIndex: Int? = null,
        currentAuctionRound: Int? = null,
        createdAt: Instant = Instant.parse("2025-01-01T00:00:00Z"),
        updatedAt: Instant = Instant.parse("2025-01-01T00:00:00Z"),
    ) = Room(
        roomId = roomId,
        code = code,
        hostId = hostId,
        status = status,
        mode = mode,
        teamCount = teamCount,
        teamSize = teamSize,
        budget = budget,
        draftOrderStrategy = draftOrderStrategy,
        currentTurnIndex = currentTurnIndex,
        currentAuctionRound = currentAuctionRound,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun roomModel(
        roomId: Long,
        code: String,
        hostId: String,
        status: RoomStatus,
        mode: TeamBuildingMode,
        teamCount: Int,
        teamSize: Int,
        budget: Int?,
        draftOrderStrategy: DraftOrderStrategy?,
        currentTurnIndex: Int?,
        currentAuctionRound: Int?,
        createdAt: Instant,
        updatedAt: Instant,
    ): RoomModel =
        object : RoomModel {
            override val roomId = roomId
            override val code = code
            override val hostId = hostId
            override val status = status
            override val mode = mode
            override val teamCount = teamCount
            override val teamSize = teamSize
            override val budget = budget
            override val draftOrderStrategy = draftOrderStrategy
            override val currentTurnIndex = currentTurnIndex
            override val currentAuctionRound = currentAuctionRound
            override val createdAt = createdAt
            override val updatedAt = updatedAt
        }
}
