package com.naminhyeok.fantazzk.room

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RoomDomainPoliciesTest {
    @Nested
    inner class `방 생성 정책` {
        @Test
        fun `생성자는 모드별 설정 불변식을 강제한다`() {
            assertThatThrownBy {
                Room(
                    code = "BAD001",
                    hostId = "host-1",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 3,
                )
            }.isInstanceOf(IllegalArgumentException::class.java)

            assertThatThrownBy {
                Room(
                    code = "BAD002",
                    hostId = "host-2",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.DRAFT,
                    teamCount = 2,
                    teamSize = 3,
                    budget = 300,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `createAuction은 대기 중인 경매 방을 만든다`() {
            val room =
                Room.createAuction(
                    code = "AUC001",
                    hostId = "host-1",
                    teamCount = 2,
                    teamSize = 3,
                    budget = 300,
                )

            assertThat(room.status).isEqualTo(RoomStatus.WAITING)
            assertThat(room.mode).isEqualTo(TeamBuildingMode.AUCTION)
            assertThat(room.budget).isEqualTo(300)
            assertThat(room.draftOrderStrategy).isNull()
            assertThat(room.configuration).isEqualTo(
                TeamBuildingConfiguration.Auction(
                    teamCount = 2,
                    teamSize = 3,
                    budget = 300,
                ),
            )
            assertThat(room.progress).isEqualTo(RoomProgress.Waiting)
        }

        @Test
        fun `createDraft는 대기 중인 드래프트 방을 만든다`() {
            val room =
                Room.createDraft(
                    code = "DRF001",
                    hostId = "host-1",
                    teamCount = 2,
                    teamSize = 3,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                )

            assertThat(room.status).isEqualTo(RoomStatus.WAITING)
            assertThat(room.mode).isEqualTo(TeamBuildingMode.DRAFT)
            assertThat(room.budget).isNull()
            assertThat(room.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
            assertThat(room.configuration).isEqualTo(
                TeamBuildingConfiguration.Draft(
                    teamCount = 2,
                    teamSize = 3,
                    strategy = DraftOrderStrategy.SNAKE,
                ),
            )
            assertThat(room.progress).isEqualTo(RoomProgress.Waiting)
        }
    }

    @Nested
    inner class `참가 정책` {
        @Test
        fun `호스트 팀장은 방 설정에 맞는 초기 예산으로 생성된다`() {
            val auctionRoom = auctionRoom()
            val draftRoom = draftRoom()

            val auctionHostLeader = auctionRoom.createHostLeader("호스트")
            val draftHostLeader = draftRoom.createHostLeader("호스트")

            assertThat(auctionHostLeader.teamLeaderId).isEqualTo(auctionRoom.hostId)
            assertThat(auctionHostLeader.nickname).isEqualTo("호스트")
            assertThat(auctionHostLeader.remainingBudget).isEqualTo(300)
            assertThat(draftHostLeader.teamLeaderId).isEqualTo(draftRoom.hostId)
            assertThat(draftHostLeader.nickname).isEqualTo("호스트")
            assertThat(draftHostLeader.remainingBudget).isNull()
        }

        @Test
        fun `대기 중이고 자리가 남아 있으면 aggregate가 참가 팀장을 생성한다`() {
            val room = auctionRoom()

            val leader = room.join(teamLeaderId = "leader-2", nickname = "참가자", currentLeaderCount = 1)

            assertThat(leader.roomId).isEqualTo(room.roomId)
            assertThat(leader.teamLeaderId).isEqualTo("leader-2")
            assertThat(leader.nickname).isEqualTo("참가자")
            assertThat(leader.remainingBudget).isEqualTo(300)
        }

        @Test
        fun `대기 중이 아닌 방에는 참가할 수 없다`() {
            val room = auctionRoom(status = RoomStatus.IN_PROGRESS)

            assertThatThrownBy { room.join(teamLeaderId = "leader-2", nickname = "참가자", currentLeaderCount = 1) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("대기 중인 방에서만 참가할 수 있습니다")
        }

        @Test
        fun `정원이 가득 찬 방에는 참가할 수 없다`() {
            val room = auctionRoom(teamCount = 2)

            assertThatThrownBy { room.join(teamLeaderId = "leader-3", nickname = "참가자", currentLeaderCount = 2) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("방이 가득 찼습니다")
        }

        @Test
        fun `드래프트 방 참가 팀장은 예산 없이 생성된다`() {
            val room = draftRoom()

            val leader = room.join(teamLeaderId = "leader-2", nickname = "참가자", currentLeaderCount = 1)

            assertThat(leader.remainingBudget).isNull()
        }
    }

    @Nested
    inner class `시작 정책` {
        @Test
        fun `경매 방을 시작하면 첫 라운드로 전환한다`() {
            val room =
                auctionRoom(
                    currentTurnIndex = 4,
                    currentAuctionRound = 8,
                )

            val started = room.start(leaderCount = room.teamCount)

            assertThat(started.status).isEqualTo(RoomStatus.IN_PROGRESS)
            assertThat(started.currentAuctionRound).isEqualTo(1)
            assertThat(started.currentTurnIndex).isNull()
            assertThat(started.progress).isEqualTo(RoomProgress.Auction(currentRound = 1))
        }

        @Test
        fun `드래프트 방을 시작하면 첫 턴으로 전환한다`() {
            val room =
                draftRoom(
                    currentTurnIndex = 3,
                    currentAuctionRound = 7,
                )

            val started = room.start(leaderCount = room.teamCount)

            assertThat(started.status).isEqualTo(RoomStatus.IN_PROGRESS)
            assertThat(started.currentTurnIndex).isEqualTo(0)
            assertThat(started.currentAuctionRound).isNull()
            assertThat(started.progress).isEqualTo(RoomProgress.Draft(currentTurnIndex = 0))
        }

        @Test
        fun `대기 중이 아닌 방은 시작할 수 없다`() {
            val room = auctionRoom(status = RoomStatus.COMPLETED)

            assertThatThrownBy { room.start(leaderCount = room.teamCount) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("대기 중인 방에서만 시작할 수 있습니다")
        }

        @Test
        fun `팀장 수가 모자라면 시작할 수 없다`() {
            val room = auctionRoom(teamCount = 3)

            assertThatThrownBy { room.start(leaderCount = 2) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("모든 팀장 자리가 채워져야 시작할 수 있습니다")
        }
    }

    @Nested
    inner class `진행 전이 정책` {
        @Test
        fun `경매 낙찰 정산은 다음 라운드로 진행하고 필요하면 방을 완료한다`() {
            val room =
                auctionRoom(
                    status = RoomStatus.IN_PROGRESS,
                    currentAuctionRound = 2,
                )

            val advanced = room.advanceAuction(nextRound = 3, completed = true)

            assertThat(advanced.currentAuctionRound).isEqualTo(3)
            assertThat(advanced.status).isEqualTo(RoomStatus.COMPLETED)
            assertThat(advanced.currentTurnIndex).isNull()
        }

        @Test
        fun `경매 낙찰 정산은 현재 이하 라운드로 진행할 수 없다`() {
            val room =
                auctionRoom(
                    status = RoomStatus.IN_PROGRESS,
                    currentAuctionRound = 2,
                )

            assertThatThrownBy { room.advanceAuction(nextRound = 2, completed = false) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("다음 경매 라운드는 현재보다 커야 합니다")
        }

        @Test
        fun `경매 유찰 정산은 상태를 유지한 채 다음 라운드로 이동한다`() {
            val room =
                auctionRoom(
                    status = RoomStatus.IN_PROGRESS,
                    currentAuctionRound = 2,
                )

            val advanced = room.moveAuctionTargetToNextRound(nextRound = 3)

            assertThat(advanced.currentAuctionRound).isEqualTo(3)
            assertThat(advanced.status).isEqualTo(RoomStatus.IN_PROGRESS)
            assertThat(advanced.currentTurnIndex).isNull()
        }

        @Test
        fun `경매 유찰 정산은 이전 라운드로 되감을 수 없다`() {
            val room =
                auctionRoom(
                    status = RoomStatus.IN_PROGRESS,
                    currentAuctionRound = 2,
                )

            assertThatThrownBy { room.moveAuctionTargetToNextRound(nextRound = -1) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("다음 경매 라운드는 현재보다 커야 합니다")
        }

        @Test
        fun `드래프트 픽 정산은 다음 턴으로 진행하고 필요하면 방을 완료한다`() {
            val room =
                draftRoom(
                    status = RoomStatus.IN_PROGRESS,
                    currentTurnIndex = 1,
                )

            val advanced = room.advanceDraftTurn(nextTurnIndex = 2, completed = true)

            assertThat(advanced.currentTurnIndex).isEqualTo(2)
            assertThat(advanced.status).isEqualTo(RoomStatus.COMPLETED)
            assertThat(advanced.currentAuctionRound).isNull()
        }

        @Test
        fun `드래프트 픽 정산은 현재 이하 턴으로 진행할 수 없다`() {
            val room =
                draftRoom(
                    status = RoomStatus.IN_PROGRESS,
                    currentTurnIndex = 1,
                )

            assertThatThrownBy { room.advanceDraftTurn(nextTurnIndex = 1, completed = false) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("다음 드래프트 턴은 현재보다 커야 합니다")
        }
    }

    private fun auctionRoom(
        status: RoomStatus = RoomStatus.WAITING,
        teamCount: Int = 2,
        currentTurnIndex: Int? = null,
        currentAuctionRound: Int? = null,
    ): Room =
        Room(
            code = "ROOM01",
            hostId = "host-1",
            status = status,
            mode = TeamBuildingMode.AUCTION,
            teamCount = teamCount,
            teamSize = 3,
            budget = 300,
            currentTurnIndex = currentTurnIndex,
            currentAuctionRound = currentAuctionRound,
        )

    private fun draftRoom(
        status: RoomStatus = RoomStatus.WAITING,
        teamCount: Int = 2,
        currentTurnIndex: Int? = null,
        currentAuctionRound: Int? = null,
    ): Room =
        Room(
            code = "ROOM02",
            hostId = "host-2",
            status = status,
            mode = TeamBuildingMode.DRAFT,
            teamCount = teamCount,
            teamSize = 3,
            draftOrderStrategy = DraftOrderStrategy.SNAKE,
            currentTurnIndex = currentTurnIndex,
            currentAuctionRound = currentAuctionRound,
        )
}
