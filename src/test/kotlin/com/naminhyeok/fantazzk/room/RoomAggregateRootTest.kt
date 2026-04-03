package com.naminhyeok.fantazzk.room
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RoomAggregateRootTest {
    @Test
    fun `방 생성 명세로 방 애그리거트 루트를 만들면 방 선수 호스트를 함께 초기화한다`() {
        val room =
            Room.createFromTemplate(
                code = "ROOM01",
                hostId = "host-1",
                hostNickname = "호스트",
                spec =
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
            Room.createAuction(
                code = "START1",
                hostId = "host",
                teamCount = 2,
                teamSize = 2,
                budget = 300,
            ).copy(
                roomId = 10L,
                leaders =
                    listOf(
                        RoomTeamLeader(roomId = 10L, teamLeaderId = "host", nickname = "호스트", remainingBudget = 300),
                        RoomTeamLeader(roomId = 10L, teamLeaderId = "guest", nickname = "게스트", remainingBudget = 300),
                    ),
            )

        val startedRoom = room.start()

        assertThat(startedRoom.status).isEqualTo(RoomStatus.IN_PROGRESS)
        assertThat(startedRoom.currentAuctionRound).isEqualTo(1)
    }

    @Test
    fun `방 애그리거트 루트가 경매 정산을 처리하며 선수 배정과 예산 차감을 반영한다`() {
        val room =
            Room.createAuction(
                code = "AUC01",
                hostId = "host",
                teamCount = 2,
                teamSize = 2,
                budget = 300,
            ).copy(
                roomId = 10L,
                status = RoomStatus.IN_PROGRESS,
                currentAuctionRound = 1,
                players =
                    listOf(
                        RoomPlayer(roomPlayerId = 1L, roomId = 10L, name = "선수1", displayOrder = 0),
                    ),
                leaders =
                    listOf(
                        RoomTeamLeader(
                            roomTeamLeaderId = 1L,
                            roomId = 10L,
                            teamLeaderId = "leader-A",
                            nickname = "A",
                            remainingBudget = 300,
                        ),
                        RoomTeamLeader(
                            roomTeamLeaderId = 2L,
                            roomId = 10L,
                            teamLeaderId = "leader-B",
                            nickname = "B",
                            remainingBudget = 300,
                        ),
                    ),
                bids =
                    listOf(
                        RoomBid(
                            roomBidId = 1L,
                            roomId = 10L,
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
            Room.createDraft(
                code = "DRF01",
                hostId = "host",
                teamCount = 2,
                teamSize = 2,
                draftOrderStrategy = DraftOrderStrategy.SNAKE,
            ).copy(
                roomId = 11L,
                status = RoomStatus.IN_PROGRESS,
                currentTurnIndex = 1,
                players = listOf(RoomPlayer(roomPlayerId = 1L, roomId = 11L, name = "선수2", displayOrder = 1)),
                leaders =
                    listOf(
                        RoomTeamLeader(roomTeamLeaderId = 1L, roomId = 11L, teamLeaderId = "leader-A", nickname = "A"),
                        RoomTeamLeader(roomTeamLeaderId = 2L, roomId = 11L, teamLeaderId = "leader-B", nickname = "B"),
                    ),
                members =
                    listOf(
                        RoomTeamMember(roomTeamMemberId = 1L, roomId = 11L, teamLeaderId = "leader-A", playerName = "선수1", assignOrder = 0),
                    ),
            )

        val pickedRoom = room.pick(teamLeaderId = "leader-B", playerName = "선수2")

        assertThat(pickedRoom.status).isEqualTo(RoomStatus.COMPLETED)
        assertThat(pickedRoom.players.single().status).isEqualTo(PlayerStatus.ASSIGNED)
        assertThat(pickedRoom.members.map { it.playerName }).containsExactly("선수1", "선수2")
    }

    @Test
    fun `드래프트 방 생성 명세는 방 전략 열거형으로 변환된다`() {
        val room =
            Room.createFromTemplate(
                code = "ROOM02",
                hostId = "host-1",
                hostNickname = "호스트",
                spec =
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
        assertThat(room.leaders.single().remainingBudget).isNull()
    }
}
