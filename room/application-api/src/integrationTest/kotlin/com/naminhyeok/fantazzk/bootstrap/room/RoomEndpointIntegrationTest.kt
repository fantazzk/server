package com.naminhyeok.fantazzk.bootstrap.room

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestConstructor

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RoomEndpointIntegrationTest(
    private val restTemplate: TestRestTemplate,
    private val jdbcTemplate: JdbcTemplate,
) {
    @Test
    fun `POST rooms는 방과 팀장과 선수 상태를 DB에 생성한다`() {
        val templateId =
            insertTemplate(
                name = "경매 템플릿",
                mode = "AUCTION",
                teamCount = 2,
                teamSize = 2,
                budget = 300,
            )
        insertTemplatePlayer(templateId, "선수B", 1)
        insertTemplatePlayer(templateId, "선수A", 0)

        val response =
            restTemplate.postForEntity(
                "/api/v1/rooms",
                mapOf("templateId" to templateId, "hostNickname" to "호스트"),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        val code = Regex(""""code"\s*:\s*"([A-Z0-9]{6})"""")
            .find(response.body.orEmpty())
            ?.groupValues
            ?.get(1)
            ?: error("room code not found in response: ${response.body}")
        val savedRoom = findRoomByCode(code)
        assertThat(savedRoom).isNotNull
        assertThat(savedRoom!!.status).isEqualTo("WAITING")
        assertThat(savedRoom.mode).isEqualTo("AUCTION")
        assertThat(savedRoom.teamCount).isEqualTo(2)
        assertThat(savedRoom.teamSize).isEqualTo(2)
        assertThat(savedRoom.budget).isEqualTo(300)

        val roomId = savedRoom.roomId
        val leaders = findLeadersByRoomId(roomId)
        assertThat(leaders).hasSize(1)
        assertThat(leaders.single().nickname).isEqualTo("호스트")
        assertThat(leaders.single().teamLeaderId).isEqualTo(savedRoom.hostId)
        assertThat(leaders.single().remainingBudget).isEqualTo(300)

        val players = findPlayersByRoomId(roomId)
        assertThat(players.map { it.name }).containsExactly("선수A", "선수B")
        assertThat(players.map { it.status }).containsOnly("AVAILABLE")
    }

    @Test
    fun `POST rooms join은 참가 팀장을 추가하고 기존 방과 선수 상태는 유지한다`() {
        val roomId =
            insertRoom(
                code = "JOINE1",
                hostId = "host-join",
                status = "WAITING",
                mode = "AUCTION",
                teamCount = 2,
                teamSize = 2,
                budget = 300,
            )
        insertRoomLeader(roomId, "host-join", "호스트", 300)
        insertRoomPlayer(roomId, "선수1", "AVAILABLE", 0)
        insertRoomPlayer(roomId, "선수2", "AVAILABLE", 1)

        val response =
            restTemplate.postForEntity(
                "/api/v1/rooms/{code}/join",
                mapOf("nickname" to "참가자"),
                String::class.java,
                "JOINE1",
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val savedRoom = findRoomByCode("JOINE1")
        assertThat(savedRoom).isNotNull
        assertThat(savedRoom!!.status).isEqualTo("WAITING")

        val leaders = findLeadersByRoomId(roomId)
        assertThat(leaders).hasSize(2)
        val leadersByNickname = leaders.associateBy { it.nickname }
        assertThat(leadersByNickname.keys).containsExactlyInAnyOrder("호스트", "참가자")
        assertThat(leadersByNickname.getValue("호스트")).isEqualTo(
            PersistedLeader(
                roomId = roomId,
                teamLeaderId = "host-join",
                nickname = "호스트",
                remainingBudget = 300,
            ),
        )
        assertThat(leadersByNickname.getValue("참가자").roomId).isEqualTo(roomId)
        assertThat(leadersByNickname.getValue("참가자").teamLeaderId).isNotBlank()
        assertThat(leadersByNickname.getValue("참가자").teamLeaderId).isNotEqualTo("host-join")
        assertThat(leadersByNickname.getValue("참가자").nickname).isEqualTo("참가자")
        assertThat(leadersByNickname.getValue("참가자").remainingBudget).isEqualTo(300)

        val players = findPlayersByRoomId(roomId)
        assertThat(players.map { it.name }).containsExactly("선수1", "선수2")
        assertThat(players.map { it.status }).containsOnly("AVAILABLE")
    }

    @Test
    fun `POST rooms start는 방 상태를 진행 중으로 바꾸고 기존 팀장과 선수는 유지한다`() {
        val roomId =
            insertRoom(
                code = "STARTE",
                hostId = "host-start",
                status = "WAITING",
                mode = "AUCTION",
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                currentTurnIndex = 4,
                currentAuctionRound = 8,
            )
        insertRoomLeader(roomId, "host-start", "호스트", 300)
        insertRoomLeader(roomId, "leader-2", "참가자", 250)
        insertRoomPlayer(roomId, "선수1", "AVAILABLE", 0)
        insertRoomPlayer(roomId, "선수2", "AVAILABLE", 1)

        val response =
            restTemplate.postForEntity(
                "/api/v1/rooms/{code}/start",
                null,
                String::class.java,
                "STARTE",
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val startedRoom = findRoomByCode("STARTE")
        assertThat(startedRoom).isNotNull
        assertThat(startedRoom!!.status).isEqualTo("IN_PROGRESS")
        assertThat(startedRoom.currentAuctionRound).isEqualTo(1)
        assertThat(startedRoom.currentTurnIndex).isNull()

        val leaders = findLeadersByRoomId(roomId)
        assertThat(leaders).hasSize(2)
        val leadersById = leaders.associateBy { it.teamLeaderId }
        assertThat(leadersById.keys).containsExactlyInAnyOrder("host-start", "leader-2")
        assertThat(leadersById.getValue("host-start")).isEqualTo(
            PersistedLeader(
                roomId = roomId,
                teamLeaderId = "host-start",
                nickname = "호스트",
                remainingBudget = 300,
            ),
        )
        assertThat(leadersById.getValue("leader-2")).isEqualTo(
            PersistedLeader(
                roomId = roomId,
                teamLeaderId = "leader-2",
                nickname = "참가자",
                remainingBudget = 250,
            ),
        )

        val players = findPlayersByRoomId(roomId)
        assertThat(players.map { it.name }).containsExactly("선수1", "선수2")
        assertThat(players.map { it.status }).containsOnly("AVAILABLE")
    }

    @Test
    fun `POST rooms start는 드래프트 방을 시작하면 현재 턴을 0으로 초기화하고 경매 라운드는 비운다`() {
        val roomId =
            insertRoom(
                code = "DRFT01",
                hostId = "host-draft",
                status = "WAITING",
                mode = "DRAFT",
                teamCount = 2,
                teamSize = 2,
                budget = null,
                draftOrderStrategy = "SNAKE",
                currentTurnIndex = 7,
                currentAuctionRound = 9,
            )
        insertRoomLeader(roomId, "host-draft", "호스트", null)
        insertRoomLeader(roomId, "leader-2", "참가자", null)

        val response =
            restTemplate.postForEntity(
                "/api/v1/rooms/{code}/start",
                null,
                String::class.java,
                "DRFT01",
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val startedRoom = findRoomByCode("DRFT01")
        assertThat(startedRoom).isNotNull
        assertThat(startedRoom!!.status).isEqualTo("IN_PROGRESS")
        assertThat(startedRoom.currentTurnIndex).isEqualTo(0)
        assertThat(startedRoom.currentAuctionRound).isNull()
    }

    private fun insertTemplate(
        name: String,
        mode: String,
        teamCount: Int,
        teamSize: Int,
        budget: Int?,
    ): Long =
        jdbcTemplate.queryForObject(
            """
            insert into template (name, mode, team_count, team_size, budget, draft_order_strategy)
            values (?, ?, ?, ?, ?, null)
            returning id
            """.trimIndent(),
            Long::class.java,
            name,
            mode,
            teamCount,
            teamSize,
            budget,
        )!!

    private fun insertTemplatePlayer(
        templateId: Long,
        name: String,
        displayOrder: Int,
    ) {
        jdbcTemplate.update(
            """
            insert into template_player (template_id, name, display_order)
            values (?, ?, ?)
            """.trimIndent(),
            templateId,
            name,
            displayOrder,
        )
    }

    private fun insertRoom(
        code: String,
        hostId: String,
        status: String,
        mode: String,
        teamCount: Int,
        teamSize: Int,
        budget: Int?,
        draftOrderStrategy: String? = null,
        currentTurnIndex: Int? = null,
        currentAuctionRound: Int? = null,
    ): Long =
        jdbcTemplate.queryForObject(
            """
            insert into room (
                code,
                host_id,
                status,
                mode,
                team_count,
                team_size,
                budget,
                draft_order_strategy,
                current_turn_index,
                current_auction_round
            )
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            returning id
            """.trimIndent(),
            Long::class.java,
            code,
            hostId,
            status,
            mode,
            teamCount,
            teamSize,
            budget,
            draftOrderStrategy,
            currentTurnIndex,
            currentAuctionRound,
        )!!

    private fun insertRoomLeader(
        roomId: Long,
        teamLeaderId: String,
        nickname: String,
        remainingBudget: Int?,
    ) {
        jdbcTemplate.update(
            """
            insert into room_team_leader (room_id, team_leader_id, nickname, remaining_budget)
            values (?, ?, ?, ?)
            """.trimIndent(),
            roomId,
            teamLeaderId,
            nickname,
            remainingBudget,
        )
    }

    private fun insertRoomPlayer(
        roomId: Long,
        name: String,
        status: String,
        displayOrder: Int,
    ) {
        jdbcTemplate.update(
            """
            insert into room_player (room_id, name, status, display_order)
            values (?, ?, ?, ?)
            """.trimIndent(),
            roomId,
            name,
            status,
            displayOrder,
        )
    }

    private fun findRoomByCode(code: String): PersistedRoom? =
        jdbcTemplate.query(
            """
            select
                id,
                code,
                host_id,
                status,
                mode,
                team_count,
                team_size,
                budget,
                draft_order_strategy,
                current_turn_index,
                current_auction_round
            from room
            where code = ?
            """.trimIndent(),
            { rs, _ ->
                PersistedRoom(
                    roomId = rs.getLong("id"),
                    code = rs.getString("code"),
                    hostId = rs.getString("host_id"),
                    status = rs.getString("status"),
                    mode = rs.getString("mode"),
                    teamCount = rs.getInt("team_count"),
                    teamSize = rs.getInt("team_size"),
                    budget = rs.getObject("budget", Int::class.javaObjectType),
                    draftOrderStrategy = rs.getString("draft_order_strategy"),
                    currentTurnIndex = rs.getObject("current_turn_index", Int::class.javaObjectType),
                    currentAuctionRound = rs.getObject("current_auction_round", Int::class.javaObjectType),
                )
            },
            code,
        ).singleOrNull()

    private fun findLeadersByRoomId(roomId: Long): List<PersistedLeader> =
        jdbcTemplate.query(
            """
            select room_id, team_leader_id, nickname, remaining_budget
            from room_team_leader
            where room_id = ?
            """.trimIndent(),
            { rs, _ ->
                PersistedLeader(
                    roomId = rs.getLong("room_id"),
                    teamLeaderId = rs.getString("team_leader_id"),
                    nickname = rs.getString("nickname"),
                    remainingBudget = rs.getObject("remaining_budget", Int::class.javaObjectType),
                )
            },
            roomId,
        )

    private fun findPlayersByRoomId(roomId: Long): List<PersistedPlayer> =
        jdbcTemplate.query(
            """
            select room_id, name, status, display_order
            from room_player
            where room_id = ?
            order by display_order
            """.trimIndent(),
            { rs, _ ->
                PersistedPlayer(
                    roomId = rs.getLong("room_id"),
                    name = rs.getString("name"),
                    status = rs.getString("status"),
                    displayOrder = rs.getInt("display_order"),
                )
            },
            roomId,
        )

    private data class PersistedRoom(
        val roomId: Long,
        val code: String,
        val hostId: String,
        val status: String,
        val mode: String,
        val teamCount: Int,
        val teamSize: Int,
        val budget: Int?,
        val draftOrderStrategy: String?,
        val currentTurnIndex: Int?,
        val currentAuctionRound: Int?,
    )

    private data class PersistedLeader(
        val roomId: Long,
        val teamLeaderId: String,
        val nickname: String,
        val remainingBudget: Int?,
    )

    private data class PersistedPlayer(
        val roomId: Long,
        val name: String,
        val status: String,
        val displayOrder: Int,
    )
}
