package com.naminhyeok.fantazzk.bootstrap.template

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
class TemplateEndpointIntegrationTest(
    private val restTemplate: TestRestTemplate,
    private val jdbcTemplate: JdbcTemplate,
) {
    @Test
    fun `POST templates는 템플릿과 선수 목록을 DB에 생성한다`() {
        val response =
            restTemplate.postForEntity(
                "/api/v1/templates",
                mapOf(
                    "name" to "경매 템플릿",
                    "mode" to "AUCTION",
                    "teamCount" to 2,
                    "teamSize" to 2,
                    "budget" to 300,
                    "playerNames" to listOf("선수A", "선수B"),
                ),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)

        val templateId =
            Regex(""""id"\s*:\s*(\d+)""")
                .find(response.body.orEmpty())
                ?.groupValues
                ?.get(1)
                ?.toLong()
                ?: error("template id not found in response: ${response.body}")

        val savedTemplate = findTemplate(templateId)
        assertThat(savedTemplate).isEqualTo(
            PersistedTemplate(
                templateId = templateId,
                name = "경매 템플릿",
                mode = "AUCTION",
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                draftOrderStrategy = null,
            ),
        )

        val players = findPlayers(templateId)
        assertThat(players.map { it.name }).containsExactly("선수A", "선수B")
        assertThat(players.map { it.displayOrder }).containsExactly(0, 1)
    }

    @Test
    fun `GET template detail은 선수 목록을 displayOrder 순서로 반환한다`() {
        val templateId =
            insertTemplate(
                name = "드래프트 템플릿",
                mode = "DRAFT",
                teamCount = 2,
                teamSize = 2,
                budget = null,
                draftOrderStrategy = "SNAKE",
            )
        insertTemplatePlayer(templateId, "선수B", 1)
        insertTemplatePlayer(templateId, "선수A", 0)

        val response = restTemplate.getForEntity("/api/v1/templates/{id}", String::class.java, templateId)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("\"name\":\"드래프트 템플릿\"")
        assertThat(response.body).contains("\"players\":[{\"name\":\"선수A\",\"displayOrder\":0},{\"name\":\"선수B\",\"displayOrder\":1}]")
    }

    @Test
    fun `GET templates는 기본 메타데이터 목록을 반환한다`() {
        insertTemplate(
            name = "첫째 템플릿",
            mode = "AUCTION",
            teamCount = 2,
            teamSize = 2,
            budget = 300,
            draftOrderStrategy = null,
        )
        insertTemplate(
            name = "둘째 템플릿",
            mode = "DRAFT",
            teamCount = 2,
            teamSize = 2,
            budget = null,
            draftOrderStrategy = "FIXED",
        )

        val response = restTemplate.getForEntity("/api/v1/templates", String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("\"name\":\"첫째 템플릿\"")
        assertThat(response.body).contains("\"name\":\"둘째 템플릿\"")
    }

    @Test
    fun `POST templates는 드래프트 요청이 예산을 포함하면 400을 반환한다`() {
        val response =
            restTemplate.postForEntity(
                "/api/v1/templates",
                mapOf(
                    "name" to "실패 템플릿",
                    "mode" to "DRAFT",
                    "teamCount" to 2,
                    "teamSize" to 2,
                    "budget" to 300,
                    "draftOrderStrategy" to "SNAKE",
                    "playerNames" to listOf("선수1", "선수2"),
                ),
                String::class.java,
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body).contains("드래프트 템플릿에는 예산을 지정할 수 없습니다")
    }

    @Test
    fun `GET template detail은 invalid 설정의 레거시 템플릿이면 409를 반환한다`() {
        val templateId =
            insertTemplate(
                name = "깨진 경매 템플릿",
                mode = "AUCTION",
                teamCount = 2,
                teamSize = 2,
                budget = null,
                draftOrderStrategy = null,
            )
        insertTemplatePlayer(templateId, "선수A", 0)
        insertTemplatePlayer(templateId, "선수B", 1)

        val response = restTemplate.getForEntity("/api/v1/templates/{id}", String::class.java, templateId)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(response.body).contains("유효하지 않은 템플릿입니다")
    }

    @Test
    fun `GET template detail은 선수 수가 exact count를 만족하지 않으면 409를 반환한다`() {
        val templateId =
            insertTemplate(
                name = "선수 부족 템플릿",
                mode = "AUCTION",
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                draftOrderStrategy = null,
            )
        insertTemplatePlayer(templateId, "선수A", 0)

        val response = restTemplate.getForEntity("/api/v1/templates/{id}", String::class.java, templateId)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(response.body).contains("유효하지 않은 템플릿입니다")
    }

    private fun insertTemplate(
        name: String,
        mode: String,
        teamCount: Int,
        teamSize: Int,
        budget: Int?,
        draftOrderStrategy: String?,
    ): Long =
        jdbcTemplate.queryForObject(
            """
            insert into template (name, mode, team_count, team_size, budget, draft_order_strategy)
            values (?, ?, ?, ?, ?, ?)
            returning id
            """.trimIndent(),
            Long::class.java,
            name,
            mode,
            teamCount,
            teamSize,
            budget,
            draftOrderStrategy,
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

    private fun findTemplate(templateId: Long): PersistedTemplate? =
        jdbcTemplate.query(
            """
            select id, name, mode, team_count, team_size, budget, draft_order_strategy
            from template
            where id = ?
            """.trimIndent(),
            { rs, _ ->
                PersistedTemplate(
                    templateId = rs.getLong("id"),
                    name = rs.getString("name"),
                    mode = rs.getString("mode"),
                    teamCount = rs.getInt("team_count"),
                    teamSize = rs.getInt("team_size"),
                    budget = rs.getObject("budget") as Int?,
                    draftOrderStrategy = rs.getString("draft_order_strategy"),
                )
            },
            templateId,
        ).singleOrNull()

    private fun findPlayers(templateId: Long): List<PersistedPlayer> =
        jdbcTemplate.query(
            """
            select template_id, name, display_order
            from template_player
            where template_id = ?
            order by display_order asc
            """.trimIndent(),
            { rs, _ ->
                PersistedPlayer(
                    templateId = rs.getLong("template_id"),
                    name = rs.getString("name"),
                    displayOrder = rs.getInt("display_order"),
                )
            },
            templateId,
        )

    private data class PersistedTemplate(
        val templateId: Long,
        val name: String,
        val mode: String,
        val teamCount: Int,
        val teamSize: Int,
        val budget: Int?,
        val draftOrderStrategy: String?,
    )

    private data class PersistedPlayer(
        val templateId: Long,
        val name: String,
        val displayOrder: Int,
    )
}
