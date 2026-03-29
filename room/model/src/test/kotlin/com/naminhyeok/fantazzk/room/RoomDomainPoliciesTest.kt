package com.naminhyeok.fantazzk.room

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RoomDomainPoliciesTest {
    @Nested
    inner class `드래프트 보드 정책` {
        @Test
        fun `SNAKE 전략은 짝수 라운드에서 역순으로 다음 픽 팀장을 계산한다`() {
            val board =
                DraftBoard(
                    teamLeaderIds = listOf("A", "B", "C"),
                    strategy = DraftOrderStrategy.SNAKE,
                    picksPerTeam = 2,
                )

            assertThat(board.pickOrder()).containsExactly("A", "B", "C", "C", "B", "A")
            assertThat(board.currentTeamLeader(turnIndex = 4)).isEqualTo("B")
        }

        @Test
        fun `FIXED 전략은 매 라운드 같은 순서를 유지한다`() {
            val board =
                DraftBoard(
                    teamLeaderIds = listOf("A", "B"),
                    strategy = DraftOrderStrategy.FIXED,
                    picksPerTeam = 3,
                )

            assertThat(board.pickOrder()).containsExactly("A", "B", "A", "B", "A", "B")
        }

        @Test
        fun `현재 턴이 전체 픽 수를 넘기면 더 이상 팀장을 조회할 수 없다`() {
            val board =
                DraftBoard(
                    teamLeaderIds = listOf("A", "B"),
                    strategy = DraftOrderStrategy.FIXED,
                    picksPerTeam = 1,
                )

            assertThatThrownBy { board.currentTeamLeader(turnIndex = 2) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("드래프트가 이미 종료되었습니다")
        }

        @Test
        fun `현재 턴이 아닌 팀장은 픽할 수 없다`() {
            val board =
                DraftBoard(
                    teamLeaderIds = listOf("A", "B"),
                    strategy = DraftOrderStrategy.SNAKE,
                    picksPerTeam = 2,
                )

            assertThatThrownBy { board.requireTurnOwner(turnIndex = 1, teamLeaderId = "A") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("현재 턴이 아닙니다")
        }

        @Test
        fun `픽 정산 정책은 다음 턴과 방 완료 여부를 함께 계산한다`() {
            val board =
                DraftBoard(
                    teamLeaderIds = listOf("A", "B"),
                    strategy = DraftOrderStrategy.SNAKE,
                    picksPerTeam = 2,
                )

            val settlement = board.settlePick(turnIndex = 2, assignedCountAfterPick = 4)

            assertThat(settlement.nextTurnIndex).isEqualTo(3)
            assertThat(settlement.completed).isTrue()
        }
    }

    @Nested
    inner class `경매 라운드 정책` {
        @Test
        fun `현재 최고가보다 낮거나 같은 금액은 입찰할 수 없다`() {
            val round =
                AuctionRound(
                    round = 3,
                    highestBid = RoomBid(roomId = 1L, round = 3, teamLeaderId = "leader-A", amount = 100),
                )

            assertThatThrownBy { round.requireHigherBid(amount = 100) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("현재 최고가보다 높아야 합니다")
        }

        @Test
        fun `최고 입찰이 있으면 낙찰 정산 정책을 만든다`() {
            val winningBid = RoomBid(roomId = 1L, round = 3, teamLeaderId = "leader-B", amount = 150)
            val round = AuctionRound(round = 3, highestBid = winningBid)

            val settlement = round.settle(playerName = "선수1", assignedCountAfterSettlement = 4, totalRequired = 4)

            assertThat(settlement.playerName).isEqualTo("선수1")
            assertThat(settlement.outcome).isEqualTo(AuctionOutcome.SOLD)
            assertThat(settlement.nextRound).isEqualTo(4)
            assertThat(settlement.completed).isTrue()
            assertThat(settlement.winningBid).isEqualTo(winningBid)
        }

        @Test
        fun `최고 입찰이 없으면 유찰 정산 정책을 만든다`() {
            val round = AuctionRound(round = 3, highestBid = null)

            val settlement = round.settle(playerName = "선수1", assignedCountAfterSettlement = 1, totalRequired = 4)

            assertThat(settlement.playerName).isEqualTo("선수1")
            assertThat(settlement.outcome).isEqualTo(AuctionOutcome.PASSED)
            assertThat(settlement.nextRound).isEqualTo(4)
            assertThat(settlement.completed).isFalse()
            assertThat(settlement.winningBid).isNull()
        }

        @Test
        fun `낙찰 팀의 정원이 가득 차면 더 이상 선수를 배정할 수 없다`() {
            val round = AuctionRound(round = 3, highestBid = RoomBid(roomId = 1L, round = 3, teamLeaderId = "leader-B", amount = 150))

            assertThatThrownBy { round.requireRosterCapacity(currentMemberCount = 2, picksPerTeam = 2) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("팀장의 팀원 정원이 가득 찼습니다")
        }
    }

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
