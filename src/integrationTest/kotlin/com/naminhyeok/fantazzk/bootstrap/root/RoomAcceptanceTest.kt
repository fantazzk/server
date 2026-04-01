package com.naminhyeok.fantazzk.bootstrap.root

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestConstructor
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class RoomAcceptanceTest(
    private val restTemplate: TestRestTemplate,
) {
    private val objectMapper = ObjectMapper()

    @Test
    fun `루트 애플리케이션 공개 API로 템플릿 생성부터 방 시작 후 조회까지 완료한다`() {
        val suffix = UUID.randomUUID().toString().take(8)
        val hostNickname = "호스트-$suffix"
        val participantNickname = "참가자-$suffix"

        val createTemplateResponse =
            restTemplate.postForEntity(
                "/api/v1/templates",
                mapOf(
                    "name" to "경매 템플릿 $suffix",
                    "mode" to "AUCTION",
                    "teamCount" to 2,
                    "teamSize" to 2,
                    "budget" to 500,
                    "playerNames" to listOf("선수A-$suffix", "선수B-$suffix"),
                ),
                String::class.java,
            )

        assertThat(createTemplateResponse.statusCode).isEqualTo(HttpStatus.CREATED)
        val createdTemplate = successBodyOf(createTemplateResponse)
        val templateId = createdTemplate.path("id").asLong()
        assertThat(templateId).isGreaterThan(0L)
        assertThat(createdTemplate.path("name").asText()).isEqualTo("경매 템플릿 $suffix")
        assertThat(createdTemplate.path("mode").asText()).isEqualTo("AUCTION")
        assertThat(createdTemplate.path("teamCount").asInt()).isEqualTo(2)
        assertThat(createdTemplate.path("teamSize").asInt()).isEqualTo(2)
        assertThat(createdTemplate.path("budget").asInt()).isEqualTo(500)

        val createRoomResponse =
            restTemplate.postForEntity(
                "/api/v1/rooms",
                mapOf(
                    "templateId" to templateId,
                    "hostNickname" to hostNickname,
                ),
                String::class.java,
            )

        assertThat(createRoomResponse.statusCode).isEqualTo(HttpStatus.CREATED)
        val createdRoom = successBodyOf(createRoomResponse)
        val roomCode = createdRoom.path("code").asText()
        assertThat(roomCode).matches("[A-Z0-9]{6}")
        assertThat(createdRoom.path("status").asText()).isEqualTo("WAITING")
        val createdLeaders = teamLeaders(createdRoom)
        assertThat(createdLeaders).hasSize(1)
        assertThat(createdLeaders.map { it.path("nickname").asText() }).containsExactly(hostNickname)
        val createdLeadersByNickname = teamLeadersByNickname(createdRoom)
        assertThat(createdLeadersByNickname.keys).containsExactly(hostNickname)
        assertThat(createdLeadersByNickname.getValue(hostNickname).path("remainingBudget").asInt()).isEqualTo(500)

        val joinRoomResponse =
            restTemplate.postForEntity(
                "/api/v1/rooms/{code}/join",
                mapOf("nickname" to participantNickname),
                String::class.java,
                roomCode,
            )

        assertThat(joinRoomResponse.statusCode).isEqualTo(HttpStatus.OK)
        val joinedRoom = successBodyOf(joinRoomResponse)
        assertThat(joinedRoom.path("code").asText()).isEqualTo(roomCode)
        assertThat(joinedRoom.path("status").asText()).isEqualTo("WAITING")
        val joinedLeaders = teamLeaders(joinedRoom)
        assertThat(joinedLeaders).hasSize(2)
        assertThat(joinedLeaders.map { it.path("nickname").asText() }).containsExactlyInAnyOrder(hostNickname, participantNickname)
        val joinedLeadersByNickname = teamLeadersByNickname(joinedRoom)
        assertThat(joinedLeadersByNickname.keys).containsExactlyInAnyOrder(hostNickname, participantNickname)
        assertThat(joinedLeadersByNickname.values.map { it.path("remainingBudget").asInt() }).containsOnly(500)

        val startRoomResponse =
            restTemplate.postForEntity(
                "/api/v1/rooms/{code}/start",
                null,
                String::class.java,
                roomCode,
            )

        assertThat(startRoomResponse.statusCode).isEqualTo(HttpStatus.OK)
        val startedRoom = successBodyOf(startRoomResponse)
        assertThat(startedRoom.path("code").asText()).isEqualTo(roomCode)
        assertThat(startedRoom.path("status").asText()).isEqualTo("IN_PROGRESS")
        val startedLeaders = teamLeaders(startedRoom)
        assertThat(startedLeaders).hasSize(2)
        assertThat(startedLeaders.map { it.path("nickname").asText() }).containsExactlyInAnyOrder(hostNickname, participantNickname)
        val startedLeadersByNickname = teamLeadersByNickname(startedRoom)
        assertThat(startedLeadersByNickname.keys).containsExactlyInAnyOrder(hostNickname, participantNickname)

        val getRoomResponse =
            eventuallyGetRoom(roomCode)

        assertThat(getRoomResponse.statusCode).isEqualTo(HttpStatus.OK)
        val foundRoom = successBodyOf(getRoomResponse)
        assertThat(foundRoom.path("code").asText()).isEqualTo(roomCode)
        assertThat(foundRoom.path("status").asText()).isEqualTo("IN_PROGRESS")
        val foundLeaders = teamLeaders(foundRoom)
        assertThat(foundLeaders).hasSize(2)
        assertThat(foundLeaders.map { it.path("nickname").asText() }).containsExactlyInAnyOrder(hostNickname, participantNickname)
        val foundLeadersByNickname = teamLeadersByNickname(foundRoom)
        assertThat(foundLeadersByNickname.keys).containsExactlyInAnyOrder(hostNickname, participantNickname)
        assertThat(foundLeadersByNickname.values.map { it.path("remainingBudget").asInt() }).containsOnly(500)
    }

    private fun bodyOf(response: ResponseEntity<String>): JsonNode {
        val body = response.body
        assertThat(body).isNotBlank()
        return objectMapper.readTree(body)
    }

    private fun successBodyOf(response: ResponseEntity<String>): JsonNode {
        val body = bodyOf(response)
        assertThat(body.path("resultType").asText()).isEqualTo("SUCCESS")
        val success = body.path("success")
        assertThat(success.isMissingNode).isFalse()
        return success
    }

    private fun teamLeaders(room: JsonNode): List<JsonNode> {
        val teamLeaders = room.path("teamLeaders")
        assertThat(teamLeaders.isArray).isTrue()
        return teamLeaders.toList()
    }

    private fun teamLeadersByNickname(room: JsonNode): Map<String, JsonNode> =
        teamLeaders(room).associateBy { it.path("nickname").asText() }

    private fun eventuallyGetRoom(code: String): ResponseEntity<String> {
        repeat(30) {
            val response =
                restTemplate.getForEntity(
                    "/api/v1/rooms/{code}",
                    String::class.java,
                    code,
                )

            if (response.statusCode == HttpStatus.OK) {
                val body = successBodyOf(response)
                if (body.path("status").asText() == "IN_PROGRESS" && teamLeaders(body).size == 2) {
                    return response
                }
            }

            Thread.sleep(100)
        }

        return restTemplate.getForEntity(
            "/api/v1/rooms/{code}",
            String::class.java,
            code,
        )
    }
}
