package com.naminhyeok.fantazzk.room.repository

import com.naminhyeok.fantazzk.room.domain.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.domain.Room
import com.naminhyeok.fantazzk.room.domain.RoomBid
import com.naminhyeok.fantazzk.room.domain.RoomId
import com.naminhyeok.fantazzk.room.domain.RoomPlayer
import com.naminhyeok.fantazzk.room.domain.RoomStatus
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader
import com.naminhyeok.fantazzk.room.domain.RoomTeamMember
import com.naminhyeok.fantazzk.room.domain.TeamBuildingMode
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
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
    private val entityManager: EntityManager,
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
                            RoomPlayer("선수1", 0),
                            RoomPlayer("선수2", 1),
                        ),
                    leaders =
                        listOf(
                            RoomTeamLeader("leader-A", "팀장A", 250),
                            RoomTeamLeader("leader-B", "팀장B", 300),
                        ),
                    members =
                        listOf(
                            RoomTeamMember("leader-A", "선수1", 0),
                        ),
                    bids =
                        listOf(
                            RoomBid(1, "leader-A", 90),
                            RoomBid(2, "leader-A", 120),
                            RoomBid(2, "leader-B", 150),
                        ),
                ),
            )

        entityManager.flush()
        entityManager.clear()

        val foundByCode = cut.findByCode("RMCH01")
        entityManager.clear()
        val foundById = cut.findById(RoomId(saved.roomId))

        assertThat(foundByCode).isNotNull
        assertThat(foundById).isNotNull
        assertThat(foundByCode).isNotSameAs(saved)
        assertThat(foundById).isNotSameAs(saved)

        listOf(foundByCode!!, foundById!!).forEach { found ->
            assertThat(found.players).hasSize(2)
            assertThat(found.players.map { it.name to it.displayOrder })
                .containsExactly("선수1" to 0, "선수2" to 1)
            assertThat(found.players.map { it.id }).allSatisfy { id ->
                assertThat(id).isNotNull
                assertThat(id!!.value).isPositive()
            }

            assertThat(found.leaders).hasSize(2)
            assertThat(found.leaders.map { it.teamLeaderId to it.remainingBudget })
                .containsExactlyInAnyOrder("leader-A" to 250, "leader-B" to 300)
            assertThat(found.leaders.map { it.id }).allSatisfy { id ->
                assertThat(id).isNotNull
                assertThat(id!!.value).isPositive()
            }

            assertThat(found.members).hasSize(1)
            assertThat(found.members.single().playerName).isEqualTo("선수1")
            assertThat(found.members.single().teamLeaderId).isEqualTo("leader-A")
            assertThat(found.members.single().id).isNotNull
            assertThat(found.members.single().id!!.value).isPositive()

            assertThat(found.bids).hasSize(2)
            assertThat(found.bids.map { it.teamLeaderId to it.amount })
                .containsExactlyInAnyOrder("leader-A" to 120, "leader-B" to 150)
            assertThat(found.bids.map { it.round }).containsOnly(2)
            assertThat(found.bids.map { it.id }).allSatisfy { id ->
                assertThat(id).isNotNull
                assertThat(id!!.value).isPositive()
            }
        }

        val totalBidRows =
            jdbcTemplate.queryForObject(
                "select count(*) from room_bid where room_id = ?",
                Int::class.java,
                saved.roomId,
            )
        assertThat(totalBidRows).isEqualTo(3)
    }

    @Test
    fun `드래프트 방 조회는 모드와 맞지 않는 budget row를 허용하지 않는다`() {
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

        assertThatThrownBy { cut.findByCode("RM0005") }
            .isInstanceOf(InvalidDataAccessApiUsageException::class.java)
            .hasMessage("드래프트 방에는 예산이 있으면 안 됩니다")
    }

    @Test
    fun `경매 방 조회는 모드와 맞지 않는 draft strategy row를 허용하지 않는다`() {
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
        assertThatThrownBy { cut.findById(RoomId(roomId)) }
            .isInstanceOf(InvalidDataAccessApiUsageException::class.java)
            .hasMessage("경매 방에는 드래프트 순서 전략이 있으면 안 됩니다")
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
