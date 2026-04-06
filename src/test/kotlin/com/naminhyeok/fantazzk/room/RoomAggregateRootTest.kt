@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.RoomTemplateSpec
import com.naminhyeok.fantazzk.room.domain.*
import com.naminhyeok.fantazzk.room.support.bidFixture
import com.naminhyeok.fantazzk.room.support.copyRoom
import com.naminhyeok.fantazzk.room.support.leaderFixture
import com.naminhyeok.fantazzk.room.support.memberFixture
import com.naminhyeok.fantazzk.room.support.playerFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RoomAggregateRootTest {
    @Test
    fun `방 생성 명세로 방 애그리거트 루트를 만들면 방 선수 호스트를 함께 초기화한다`() {
        val room =
            Room.createFromTemplate(
                "ROOM01",
                "host-1",
                "호스트",
                RoomTemplateSpec(
                    mode = RoomTemplateSpec.Mode.AUCTION,
                    teamCount = 2,
                    teamSize = 3,
                    budget = 300,
                    draftOrderStrategy = null,
                    players =
                        listOf(
                            RoomTemplateSpec.Player(name = "선수B", displayOrder = 1),
                            RoomTemplateSpec.Player(name = "선수A", displayOrder = 0),
                        ),
                ),
            )

        assertThat(room.code).isEqualTo("ROOM01")
        assertThat(room.status).isEqualTo(RoomStatus.WAITING)
        assertThat(room.mode).isEqualTo(TeamBuildingMode.AUCTION)
        assertThat(room.players.map { it.name }).containsExactly("선수A", "선수B")
        val hostLeader = room.leaders.single()
        assertThat(hostLeader.teamLeaderId).isEqualTo("host-1")
        assertThat(hostLeader.nickname).isEqualTo("호스트")
        assertThat(hostLeader.remainingBudget).isEqualTo(300)
    }

    @Test
    fun `방 애그리거트 루트가 방 시작을 처리하며 진행 상태를 초기화한다`() {
        val room =
            copyRoom(
                Room.createAuction(
                    "START1",
                    "host",
                    2,
                    2,
                    300,
                ),
                roomId = RoomId(10L),
                leaders =
                    listOf(
                        leaderFixture(roomId = RoomId(10L), teamLeaderId = "host", nickname = "호스트", remainingBudget = 300),
                        leaderFixture(roomId = RoomId(10L), teamLeaderId = "guest", nickname = "게스트", remainingBudget = 300),
                    ),
            )

        val startedRoom = room.start()

        assertThat(startedRoom.status).isEqualTo(RoomStatus.IN_PROGRESS)
        assertThat(startedRoom.currentAuctionRound).isEqualTo(1)
    }

    @Test
    fun `방 애그리거트 루트가 경매 정산을 처리하며 선수 배정과 예산 차감을 반영한다`() {
        val room =
            copyRoom(
                Room.createAuction(
                    "AUC01",
                    "host",
                    2,
                    2,
                    300,
                ),
                roomId = RoomId(10L),
                status = RoomStatus.IN_PROGRESS,
                currentAuctionRound = 1,
                players =
                    listOf(
                        playerFixture(roomPlayerId = roomPlayerId(1L), roomId = RoomId(10L), name = "선수1", displayOrder = 0),
                    ),
                leaders =
                    listOf(
                        leaderFixture(
                            roomTeamLeaderId = roomTeamLeaderId(1L),
                            roomId = RoomId(10L),
                            teamLeaderId = "leader-A",
                            nickname = "A",
                            remainingBudget = 300,
                        ),
                        leaderFixture(
                            roomTeamLeaderId = roomTeamLeaderId(2L),
                            roomId = RoomId(10L),
                            teamLeaderId = "leader-B",
                            nickname = "B",
                            remainingBudget = 300,
                        ),
                    ),
                bids =
                    listOf(
                        bidFixture(
                            roomBidId = roomBidId(1L),
                            roomId = RoomId(10L),
                            round = 1,
                            teamLeaderId = "leader-B",
                            amount = 150,
                        ),
                    ),
            )

        val settledRoom = room.settleAuction()

        assertThat(settledRoom.players.single().status).isEqualTo(PlayerStatus.ASSIGNED)
        val assignedMember = settledRoom.members.single()
        assertThat(assignedMember.teamLeaderId).isEqualTo("leader-B")
        assertThat(assignedMember.playerName).isEqualTo("선수1")
        assertThat(settledRoom.leaders.single { it.teamLeaderId == "leader-B" }.remainingBudget).isEqualTo(150)
    }

    @Test
    fun `방 애그리거트 루트가 마지막 드래프트 지명을 처리하면 방을 완료한다`() {
        val room =
            copyRoom(
                Room.createDraft(
                    "DRF01",
                    "host",
                    2,
                    2,
                    DraftOrderStrategy.SNAKE,
                ),
                roomId = RoomId(11L),
                status = RoomStatus.IN_PROGRESS,
                currentTurnIndex = 1,
                players =
                    listOf(
                        playerFixture(
                            roomPlayerId = roomPlayerId(1L),
                            roomId = RoomId(11L),
                            name = "선수2",
                            displayOrder = 1,
                        ),
                    ),
                leaders =
                    listOf(
                        leaderFixture(
                            roomTeamLeaderId = roomTeamLeaderId(1L),
                            roomId = RoomId(11L),
                            teamLeaderId = "leader-A",
                            nickname = "A",
                        ),
                        leaderFixture(
                            roomTeamLeaderId = roomTeamLeaderId(2L),
                            roomId = RoomId(11L),
                            teamLeaderId = "leader-B",
                            nickname = "B",
                        ),
                    ),
                members =
                    listOf(
                        memberFixture(
                            roomTeamMemberId = roomTeamMemberId(1L),
                            roomId = RoomId(11L),
                            teamLeaderId = "leader-A",
                            playerName = "선수1",
                            assignOrder = 0,
                        ),
                    ),
            )

        val pickedRoom = room.pick("leader-B", "선수2")

        assertThat(pickedRoom.status).isEqualTo(RoomStatus.COMPLETED)
        assertThat(pickedRoom.players.single().status).isEqualTo(PlayerStatus.ASSIGNED)
        assertThat(pickedRoom.members.map { it.playerName }).containsExactly("선수1", "선수2")
    }

    @Test
    fun `드래프트 방 생성 명세는 방 전략 열거형으로 변환된다`() {
        val room =
            Room.createFromTemplate(
                "ROOM02",
                "host-1",
                "호스트",
                RoomTemplateSpec(
                    mode = RoomTemplateSpec.Mode.DRAFT,
                    teamCount = 2,
                    teamSize = 2,
                    budget = null,
                    draftOrderStrategy = RoomTemplateSpec.DraftOrderStrategy.SNAKE,
                    players = listOf(RoomTemplateSpec.Player(name = "선수A", displayOrder = 0)),
                ),
            )

        assertThat(room.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
        assertThat(nullable(room.leaders.single().remainingBudget)).isNull()
    }
}
