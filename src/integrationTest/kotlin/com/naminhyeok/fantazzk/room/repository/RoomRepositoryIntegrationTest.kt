package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.RoomId
import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.domain.PlayerStatus
import com.naminhyeok.fantazzk.room.domain.Room
import com.naminhyeok.fantazzk.room.domain.RoomBid
import com.naminhyeok.fantazzk.room.domain.RoomPlayer
import com.naminhyeok.fantazzk.room.domain.RoomStatus
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader
import com.naminhyeok.fantazzk.room.domain.RoomTeamMember
import com.naminhyeok.fantazzk.room.domain.TeamBuildingMode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestConstructor
import java.time.Instant
import java.util.UUID

@ImportAutoConfiguration(
    LiquibaseAutoConfiguration::class,
)
@Import(RoomRepositoryAdapter::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RoomRepositoryIntegrationTest(
    private val cut: Rooms,
    private val jdbcTemplate: JdbcTemplate,
) {
    @Test
    fun `방을 저장하고 코드로 조회할 수 있다`() {
        val saved = cut.save(auctionRoom(code = "RM0001"))

        assertThatCode { UUID.fromString(saved.roomId.toString()) }.doesNotThrowAnyException()

        val found = cut.findByCode("RM0001")
        assertThat(found).isNotNull
        assertThat(found!!.status).isEqualTo(RoomStatus.WAITING)
        assertThat(found.mode).isEqualTo(TeamBuildingMode.AUCTION)
    }

    @Test
    fun `방을 저장하고 ID로 조회할 수 있다`() {
        val saved =
            cut.save(
                draftRoom(
                    code = "RM0002",
                    teamCount = 3,
                    teamSize = 4,
                    currentTurnIndex = 0,
                ),
            )

        val found = cut.findById(saved.roomId)
        assertThat(found).isNotNull
        assertThat(found!!.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
        assertThat(found.currentTurnIndex).isEqualTo(0)
    }

    @Test
    fun `존재하지 않는 방 코드는 조회하면 null을 반환한다`() {
        assertThat(cut.findByCode("NO_ROOM")).isNull()
    }

    @Test
    fun `존재하지 않는 방 ID는 조회하면 null을 반환한다`() {
        assertThat(cut.findById(RoomId.from(UUID.randomUUID()))).isNull()
    }

    @Test
    fun `경매 방을 저장하면 nullable 드래프트 필드는 null로 유지된다`() {
        val saved = cut.save(auctionRoom(code = "RM0003"))

        val found = cut.findById(saved.roomId)
        assertThat(found).isNotNull
        assertThat(found!!.budget).isEqualTo(300)
        assertThat(found.draftOrderStrategy).isNull()
        assertThat(found.currentTurnIndex).isNull()
        assertThat(found.currentAuctionRound).isNull()
    }

    @Test
    fun `aggregate 를 저장하면 코드와 ID 조회 모두 자식 컬렉션까지 재수화한다`() {
        val roomId = RoomId.random()
        val saved =
            cut.save(
                auctionRoom(
                    roomId = roomId,
                    code = "RMCH01",
                    status = RoomStatus.IN_PROGRESS,
                    currentAuctionRound = 2,
                    players =
                        listOf(
                            roomPlayer(roomId, "선수1", 0),
                            roomPlayer(roomId, "선수2", 1),
                        ),
                    leaders =
                        listOf(
                            roomTeamLeader(roomId, "leader-A", "팀장A", remainingBudget = 250),
                            roomTeamLeader(roomId, "leader-B", "팀장B", remainingBudget = 300),
                        ),
                    members =
                        listOf(
                            roomTeamMember(roomId, "leader-A", "선수1", assignOrder = 0),
                        ),
                    bids =
                        listOf(
                            roomBid(roomId, round = 1, teamLeaderId = "leader-A", amount = 90),
                            roomBid(roomId, round = 2, teamLeaderId = "leader-A", amount = 120),
                            roomBid(roomId, round = 2, teamLeaderId = "leader-B", amount = 150),
                        ),
                ),
            )

        val foundByCode = cut.findByCode("RMCH01")
        val foundById = cut.findById(saved.roomId)

        assertThat(foundByCode).isNotNull
        assertThat(foundById).isNotNull

        listOf(foundByCode!!, foundById!!).forEach { found ->
            assertThat(found.players).hasSize(2)
            assertThat(found.players.map { it.name to it.displayOrder })
                .containsExactly("선수1" to 0, "선수2" to 1)

            assertThat(found.leaders).hasSize(2)
            assertThat(found.leaders.map { it.teamLeaderId to it.remainingBudget })
                .containsExactlyInAnyOrder("leader-A" to 250, "leader-B" to 300)

            assertThat(found.members).hasSize(1)
            assertThat(found.members.single().playerName).isEqualTo("선수1")
            assertThat(found.members.single().teamLeaderId).isEqualTo("leader-A")

            assertThat(found.bids).hasSize(2)
            assertThat(found.bids.map { it.teamLeaderId to it.amount })
                .containsExactlyInAnyOrder("leader-A" to 120, "leader-B" to 150)
            assertThat(found.bids.map { it.round }).containsOnly(2)
        }

        val totalBidRows =
            jdbcTemplate.queryForObject(
                "select count(*) from room_bid where room_id = ?",
                Int::class.java,
                saved.roomId.value,
            )
        assertThat(totalBidRows).isEqualTo(3)
    }

    @Test
    fun `드래프트 방 조회는 모드와 맞지 않는 budget row를 허용하지 않는다`() {
        val roomId = UUID.randomUUID()
        jdbcTemplate.update(
            """
            INSERT INTO room (
                id, code, host_id, status, mode, team_count, team_size,
                budget, draft_order_strategy, current_turn_index, current_auction_round,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            roomId,
            "RM0005",
            "host",
            RoomStatus.WAITING.name,
            TeamBuildingMode.DRAFT.name,
            2,
            2,
            300,
            DraftOrderStrategy.SNAKE.name,
            null,
            null,
            java.sql.Timestamp.from(java.time.Instant.parse("2025-01-01T00:00:00Z")),
            java.sql.Timestamp.from(java.time.Instant.parse("2025-01-01T00:00:00Z")),
        )

        assertThatThrownBy { cut.findByCode("RM0005") }
            .isInstanceOf(InvalidDataAccessApiUsageException::class.java)
            .hasMessage("드래프트 방에는 예산이 있으면 안 됩니다")
    }

    @Test
    fun `경매 방 조회는 모드와 맞지 않는 draft strategy row를 허용하지 않는다`() {
        val roomId = UUID.randomUUID()
        jdbcTemplate.update(
            """
            INSERT INTO room (
                id, code, host_id, status, mode, team_count, team_size,
                budget, draft_order_strategy, current_turn_index, current_auction_round,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            roomId,
            "RM0006",
            "host",
            RoomStatus.IN_PROGRESS.name,
            TeamBuildingMode.AUCTION.name,
            2,
            2,
            300,
            DraftOrderStrategy.FIXED.name,
            null,
            2,
            java.sql.Timestamp.from(java.time.Instant.parse("2025-01-01T00:00:00Z")),
            java.sql.Timestamp.from(java.time.Instant.parse("2025-01-01T00:00:00Z")),
        )

        val storedRoomId = jdbcTemplate.queryForObject("SELECT id FROM room WHERE code = ?", UUID::class.java, "RM0006")!!
        assertThatThrownBy { cut.findById(RoomId.from(storedRoomId)) }
            .isInstanceOf(InvalidDataAccessApiUsageException::class.java)
            .hasMessage("경매 방에는 드래프트 순서 전략이 있으면 안 됩니다")
    }

    @Test
    fun `방을 업데이트하면 변경한 필드가 그대로 반영된다`() {
        val saved =
            cut.save(
                draftRoom(
                    code = "RM0004",
                    currentTurnIndex = 0,
                ),
            )

        val updated =
            copyRoom(
                saved,
                status = RoomStatus.IN_PROGRESS,
                draftOrderStrategy = DraftOrderStrategy.FIXED,
                currentTurnIndex = 3,
                currentAuctionRound = null,
            )
        cut.save(updated)

        val found = cut.findByCode("RM0004")
        assertThat(found!!.status).isEqualTo(RoomStatus.IN_PROGRESS)
        assertThat(found.draftOrderStrategy).isEqualTo(DraftOrderStrategy.FIXED)
        assertThat(found.currentTurnIndex).isEqualTo(3)
        assertThat(found.budget).isNull()
        assertThat(found.currentAuctionRound).isNull()
    }

    private fun auctionRoom(
        roomId: RoomId = RoomId.random(),
        code: String,
        hostId: String = "host",
        status: RoomStatus = RoomStatus.WAITING,
        teamCount: Int = 2,
        teamSize: Int = 2,
        budget: Int = 300,
        currentAuctionRound: Int? = null,
        players: List<RoomPlayer> = emptyList(),
        leaders: List<RoomTeamLeader> = emptyList(),
        members: List<RoomTeamMember> = emptyList(),
        bids: List<RoomBid> = emptyList(),
    ): Room =
        Room.restore(
            roomId,
            code,
            hostId,
            status,
            TeamBuildingMode.AUCTION,
            teamCount,
            teamSize,
            budget,
            null,
            null,
            currentAuctionRound,
            players,
            leaders,
            members,
            bids,
            FIXED_INSTANT,
            FIXED_INSTANT,
        )

    private fun draftRoom(
        roomId: RoomId = RoomId.random(),
        code: String,
        hostId: String = "host",
        status: RoomStatus = RoomStatus.WAITING,
        teamCount: Int = 2,
        teamSize: Int = 2,
        draftOrderStrategy: DraftOrderStrategy = DraftOrderStrategy.SNAKE,
        currentTurnIndex: Int? = null,
        players: List<RoomPlayer> = emptyList(),
        leaders: List<RoomTeamLeader> = emptyList(),
        members: List<RoomTeamMember> = emptyList(),
        bids: List<RoomBid> = emptyList(),
    ): Room =
        Room.restore(
            roomId,
            code,
            hostId,
            status,
            TeamBuildingMode.DRAFT,
            teamCount,
            teamSize,
            null,
            draftOrderStrategy,
            currentTurnIndex,
            null,
            players,
            leaders,
            members,
            bids,
            FIXED_INSTANT,
            FIXED_INSTANT,
        )

    private fun copyRoom(
        room: Room,
        status: RoomStatus = room.status,
        draftOrderStrategy: DraftOrderStrategy? = room.draftOrderStrategy,
        currentTurnIndex: Int? = room.currentTurnIndex,
        currentAuctionRound: Int? = room.currentAuctionRound,
    ): Room =
        Room.restore(
            room.roomId,
            room.code,
            room.hostId,
            status,
            room.mode,
            room.teamCount,
            room.teamSize,
            room.budget,
            draftOrderStrategy,
            currentTurnIndex,
            currentAuctionRound,
            room.players.map(RoomPlayer::copy),
            room.leaders.map(RoomTeamLeader::copy),
            room.members.map(RoomTeamMember::copy),
            room.bidHistory().map(RoomBid::copy),
            room.createdAt,
            room.updatedAt,
        )

    private fun roomPlayer(
        roomId: RoomId,
        name: String,
        displayOrder: Int,
        status: PlayerStatus = PlayerStatus.AVAILABLE,
    ): RoomPlayer =
        RoomPlayer.restore(
            null,
            roomId,
            name,
            status,
            displayOrder,
            FIXED_INSTANT,
            FIXED_INSTANT,
        )

    private fun roomTeamLeader(
        roomId: RoomId,
        teamLeaderId: String,
        nickname: String,
        remainingBudget: Int?,
    ): RoomTeamLeader =
        RoomTeamLeader.restore(
            null,
            roomId,
            teamLeaderId,
            nickname,
            remainingBudget,
            FIXED_INSTANT,
            FIXED_INSTANT,
        )

    private fun roomTeamMember(
        roomId: RoomId,
        teamLeaderId: String,
        playerName: String,
        assignOrder: Int,
    ): RoomTeamMember =
        RoomTeamMember.restore(
            null,
            roomId,
            teamLeaderId,
            playerName,
            assignOrder,
            FIXED_INSTANT,
            FIXED_INSTANT,
        )

    private fun roomBid(
        roomId: RoomId,
        round: Int,
        teamLeaderId: String,
        amount: Int,
    ): RoomBid =
        RoomBid.restore(
            null,
            roomId,
            round,
            teamLeaderId,
            amount,
            FIXED_INSTANT,
            FIXED_INSTANT,
        )

    companion object {
        private val FIXED_INSTANT: Instant = Instant.parse("2025-01-01T00:00:00Z")
    }
}
