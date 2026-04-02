package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.RootCombinedJdbcConfiguration
import com.naminhyeok.fantazzk.room.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomBid
import com.naminhyeok.fantazzk.room.RoomId
import com.naminhyeok.fantazzk.room.RoomPlayer
import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.RoomTeamLeader
import com.naminhyeok.fantazzk.room.RoomTeamMember
import com.naminhyeok.fantazzk.room.TeamBuildingMode
import com.naminhyeok.fantazzk.room.config.RoomJdbcConfiguration
import com.naminhyeok.fantazzk.template.config.TemplateJdbcConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestConstructor

@ImportAutoConfiguration(
    LiquibaseAutoConfiguration::class,
    RootCombinedJdbcConfiguration::class,
    RoomJdbcConfiguration::class,
    TemplateJdbcConfiguration::class,
    RoomRepositoryConfiguration::class,
)
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RoomRepositoryIntegrationTest(
    private val cut: RoomRepository,
    private val jdbcTemplate: JdbcTemplate,
) {
    @Test
    fun `방을 저장하고 코드로 조회할 수 있다`() {
        val saved =
            cut.save(
                Room(
                    code = "RM0001",
                    hostId = "host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )

        assertThat(saved.roomId).isGreaterThan(0)

        val found = cut.findByCode("RM0001")
        assertThat(found).isNotNull
        assertThat(found!!.status).isEqualTo(RoomStatus.WAITING)
        assertThat(found.mode).isEqualTo(TeamBuildingMode.AUCTION)
    }

    @Test
    fun `방을 저장하고 ID로 조회할 수 있다`() {
        val saved =
            cut.save(
                Room(
                    code = "RM0002",
                    hostId = "host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.DRAFT,
                    teamCount = 3,
                    teamSize = 4,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                    currentTurnIndex = 0,
                ),
            )

        val found = cut.findById(RoomId(saved.roomId))
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
        assertThat(cut.findById(RoomId(Long.MAX_VALUE))).isNull()
    }

    @Test
    fun `경매 방을 저장하면 nullable 드래프트 필드는 null로 유지된다`() {
        val saved =
            cut.save(
                Room(
                    code = "RM0003",
                    hostId = "host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )

        val found = cut.findById(RoomId(saved.roomId))
        assertThat(found).isNotNull
        assertThat(found!!.budget).isEqualTo(300)
        assertThat(found.draftOrderStrategy).isNull()
        assertThat(found.currentTurnIndex).isNull()
        assertThat(found.currentAuctionRound).isNull()
    }

    @Test
    fun `aggregate 를 저장하면 코드와 ID 조회 모두 자식 컬렉션까지 재수화한다`() {
        val saved =
            cut.save(
                Room(
                    code = "RMCH01",
                    hostId = "host",
                    status = RoomStatus.IN_PROGRESS,
                    mode = TeamBuildingMode.AUCTION,
                    teamCount = 2,
                    teamSize = 3,
                    budget = 300,
                    currentAuctionRound = 2,
                    players =
                        listOf(
                            RoomPlayer(roomId = 0L, name = "선수1", displayOrder = 0),
                            RoomPlayer(roomId = 0L, name = "선수2", displayOrder = 1),
                        ),
                    leaders =
                        listOf(
                            RoomTeamLeader(roomId = 0L, teamLeaderId = "leader-A", nickname = "팀장A", remainingBudget = 250),
                            RoomTeamLeader(roomId = 0L, teamLeaderId = "leader-B", nickname = "팀장B", remainingBudget = 300),
                        ),
                    members =
                        listOf(
                            RoomTeamMember(roomId = 0L, teamLeaderId = "leader-A", playerName = "선수1", assignOrder = 0),
                        ),
                    bids =
                        listOf(
                            RoomBid(roomId = 0L, round = 2, teamLeaderId = "leader-A", amount = 120),
                            RoomBid(roomId = 0L, round = 2, teamLeaderId = "leader-B", amount = 150),
                        ),
                ),
            )

        val foundByCode = cut.findByCode("RMCH01")
        val foundById = cut.findById(RoomId(saved.roomId))

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
        }
    }

    @Test
    fun `legacy 드래프트 row는 stale budget이 있어도 코드 조회 시 정규화된다`() {
        jdbcTemplate.update(
            """
            INSERT INTO room (
                code, host_id, status, mode, team_count, team_size,
                budget, draft_order_strategy, current_turn_index, current_auction_round,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
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

        val found = cut.findByCode("RM0005")

        assertThat(found).isNotNull
        assertThat(found!!.mode).isEqualTo(TeamBuildingMode.DRAFT)
        assertThat(found.budget).isNull()
        assertThat(found.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
    }

    @Test
    fun `legacy 경매 row는 stale draft strategy가 있어도 ID 조회 시 정규화된다`() {
        jdbcTemplate.update(
            """
            INSERT INTO room (
                code, host_id, status, mode, team_count, team_size,
                budget, draft_order_strategy, current_turn_index, current_auction_round,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
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

        val roomId = jdbcTemplate.queryForObject("SELECT id FROM room WHERE code = ?", Long::class.java, "RM0006")!!
        val found = cut.findById(RoomId(roomId))

        assertThat(found).isNotNull
        assertThat(found!!.mode).isEqualTo(TeamBuildingMode.AUCTION)
        assertThat(found.budget).isEqualTo(300)
        assertThat(found.draftOrderStrategy).isNull()
        assertThat(found.currentAuctionRound).isEqualTo(2)
    }

    @Test
    fun `방을 업데이트하면 변경한 필드가 그대로 반영된다`() {
        val saved =
            cut.save(
                Room(
                    code = "RM0004",
                    hostId = "host",
                    status = RoomStatus.WAITING,
                    mode = TeamBuildingMode.DRAFT,
                    teamCount = 2,
                    teamSize = 2,
                    draftOrderStrategy = DraftOrderStrategy.SNAKE,
                    currentTurnIndex = 0,
                ),
            )

        val updated =
            saved.copy(
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
}
