package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.api.RoomApiController
import com.naminhyeok.fantazzk.room.api.RoomExceptionHandler
import com.naminhyeok.fantazzk.room.application.AuctionService
import com.naminhyeok.fantazzk.room.application.AuctionSettleResult
import com.naminhyeok.fantazzk.room.application.DraftService
import com.naminhyeok.fantazzk.room.application.RoomCreateService
import com.naminhyeok.fantazzk.room.application.RoomJoinService
import com.naminhyeok.fantazzk.room.application.RoomLookupService
import com.naminhyeok.fantazzk.room.application.RoomStartService
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.exception.RoomTemplateNotFoundException
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant

class RoomApiControllerTest {
    private val roomCreateService: RoomCreateService = mockk()
    private val roomLookupService: RoomLookupService = mockk()
    private val roomJoinService: RoomJoinService = mockk()
    private val roomStartService: RoomStartService = mockk()
    private val auctionService: AuctionService = mockk()
    private val draftService: DraftService = mockk()

    private val now = Instant.now()

    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(
                RoomApiController(
                    roomCreateService,
                    roomLookupService,
                    roomJoinService,
                    roomStartService,
                    auctionService,
                    draftService,
                ),
            )
            .setControllerAdvice(RoomExceptionHandler())
            .build()

    @Nested
    inner class `방 조회` {
        @Test
        fun `존재하는 방을 조회하면 200과 방 정보를 반환한다`() {
            val room = room("ABC123")
            every { roomLookupService.get("ABC123") } returns room

            mockMvc.get("/api/v1/rooms/ABC123")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.resultType") { value("SUCCESS") }
                    jsonPath("$.success.code") { value("ABC123") }
                    jsonPath("$.success.status") { value("WAITING") }
                    jsonPath("$.success.teamLeaders[0].id") { value("leader-1") }
                    jsonPath("$.success.teamLeaders[0].nickname") { value("참가자") }
                    jsonPath("$.success.teamLeaders[0].remainingBudget") { value(300) }
                    jsonPath("$.error") { doesNotExist() }
                }
        }

        @Test
        fun `존재하지 않는 방을 조회하면 404를 반환한다`() {
            every { roomLookupService.get("NOCODE") } throws RoomException.RoomNotFoundException()

            mockMvc.get("/api/v1/rooms/NOCODE")
                .andExpect {
                    status { isNotFound() }
                    jsonPath("$.resultType") { value("ERROR") }
                    jsonPath("$.success") { doesNotExist() }
                    jsonPath("$.error.status") { value(404) }
                    jsonPath("$.error.errorCode") { value("ROOM_NOT_FOUND") }
                    jsonPath("$.error.reason") { value("방을 찾을 수 없습니다") }
                }
        }
    }

    @Nested
    inner class `방 생성` {
        @Test
        fun `유효한 요청으로 방을 생성하면 요청 본문을 서비스 인자로 매핑하고 201을 반환한다`() {
            val room = room("NEW001")
            val leaders = listOf(leader(room.roomId))
            every { roomCreateService.create(1L, "호스트") } returns room
            every { roomLookupService.getTeamLeaders(room.roomId) } returns leaders

            mockMvc.post("/api/v1/rooms") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"templateId": 1, "hostNickname": "호스트"}"""
            }.andExpect {
                status { isCreated() }
                jsonPath("$.resultType") { value("SUCCESS") }
                jsonPath("$.success.code") { value("NEW001") }
                jsonPath("$.success.status") { value("WAITING") }
                jsonPath("$.success.teamLeaders[0].id") { value("leader-1") }
                jsonPath("$.success.teamLeaders[0].nickname") { value("참가자") }
                jsonPath("$.success.teamLeaders[0].remainingBudget") { value(300) }
            }
        }

        @Test
        fun `템플릿이 없으면 404를 반환한다`() {
            every { roomCreateService.create(999L, "호스트") } throws RoomTemplateNotFoundException()

            mockMvc.post("/api/v1/rooms") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"templateId": 999, "hostNickname": "호스트"}"""
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.resultType") { value("ERROR") }
                jsonPath("$.error.status") { value(404) }
                jsonPath("$.error.reason") { value("템플릿을 찾을 수 없습니다") }
            }
        }

        @Test
        fun `본문이 없으면 400을 반환한다`() {
            mockMvc.post("/api/v1/rooms") {
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.resultType") { value("ERROR") }
                jsonPath("$.error.errorCode") { value("REQUEST_ERROR") }
            }
        }
    }

    @Nested
    inner class `방 참가` {
        @Test
        fun `유효한 요청으로 참가하면 경로와 본문 값을 서비스 인자로 매핑한다`() {
            val room = room("JOIN01")
            val leader = leader(room.roomId)
            every { roomJoinService.join("JOIN01", "참가자") } returns leader
            every { roomLookupService.get("JOIN01") } returns room
            every { roomLookupService.getTeamLeaders(room.roomId) } returns listOf(leader)

            mockMvc.post("/api/v1/rooms/JOIN01/join") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"nickname": "참가자"}"""
            }.andExpect {
                status { isOk() }
                jsonPath("$.resultType") { value("SUCCESS") }
                jsonPath("$.success.teamLeaders[0].nickname") { value("참가자") }
            }
        }

        @Test
        fun `존재하지 않는 방에 참가하면 404를 반환한다`() {
            every { roomJoinService.join("NOROOM", any()) } throws RoomException.RoomNotFoundException()

            mockMvc.post("/api/v1/rooms/NOROOM/join") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"nickname": "참가자"}"""
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.resultType") { value("ERROR") }
                jsonPath("$.error.errorCode") { value("ROOM_NOT_FOUND") }
            }
        }

        @Test
        fun `정원이 가득 찬 방에 참가하면 409를 반환한다`() {
            every { roomJoinService.join("FULL01", any()) } throws IllegalStateException("방이 가득 찼습니다")

            mockMvc.post("/api/v1/rooms/FULL01/join") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"nickname": "참가자"}"""
            }.andExpect {
                status { isConflict() }
                jsonPath("$.resultType") { value("ERROR") }
                jsonPath("$.error.status") { value(409) }
                jsonPath("$.error.errorCode") { value("INVALID_STATE") }
            }
        }

        @Test
        fun `잘못된 JSON 본문이면 400을 반환한다`() {
            mockMvc.post("/api/v1/rooms/JOIN01/join") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"nickname":"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.resultType") { value("ERROR") }
                jsonPath("$.error.errorCode") { value("REQUEST_ERROR") }
            }
        }
    }

    @Nested
    inner class `방 시작` {
        @Test
        fun `성공적으로 시작하면 경로 값을 서비스 인자로 매핑하고 200을 반환한다`() {
            val room = room("START1", status = RoomStatus.IN_PROGRESS)
            justRun { roomStartService.start("START1") }
            every { roomLookupService.get("START1") } returns room
            every { roomLookupService.getTeamLeaders(room.roomId) } returns emptyList()

            mockMvc.post("/api/v1/rooms/START1/start")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.resultType") { value("SUCCESS") }
                    jsonPath("$.success.status") { value("IN_PROGRESS") }
                }
        }

        @Test
        fun `상태가 유효하지 않으면 409를 반환한다`() {
            every { roomStartService.start("PROG01") } throws IllegalStateException("진행 중인 방에서만 가능합니다")

            mockMvc.post("/api/v1/rooms/PROG01/start")
                .andExpect {
                    status { isConflict() }
                    jsonPath("$.resultType") { value("ERROR") }
                    jsonPath("$.error.errorCode") { value("INVALID_STATE") }
                }
        }
    }

    @Nested
    inner class `입찰` {
        @Test
        fun `성공적으로 입찰하면 200과 방 정보를 반환한다`() {
            val room = room("BID001")
            val bid =
                RoomBid(
                    roomBidId = 1L,
                    roomId = 1L,
                    round = 1,
                    teamLeaderId = "leader-A",
                    amount = 100,
                    createdAt = now,
                    updatedAt = now,
                )
            every { auctionService.placeBid("BID001", "leader-A", 100) } returns bid
            every { roomLookupService.get("BID001") } returns room
            every { roomLookupService.getTeamLeaders(room.roomId) } returns emptyList()

            mockMvc.post("/api/v1/rooms/BID001/bid") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"teamLeaderId": "leader-A", "amount": 100}"""
            }.andExpect {
                status { isOk() }
                jsonPath("$.resultType") { value("SUCCESS") }
                jsonPath("$.success.code") { value("BID001") }
            }
        }

        @Test
        fun `예산 초과 입찰 시 400을 반환한다`() {
            every { auctionService.placeBid("BID001", "leader-A", 9999) } throws
                IllegalArgumentException("예산이 부족합니다")

            mockMvc.post("/api/v1/rooms/BID001/bid") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"teamLeaderId": "leader-A", "amount": 9999}"""
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.resultType") { value("ERROR") }
                jsonPath("$.error.errorCode") { value("BAD_REQUEST") }
            }
        }
    }

    @Nested
    inner class `정산` {
        @Test
        fun `성공적으로 정산하면 200과 방 정보를 반환한다`() {
            val room = room("SET001")
            every { auctionService.settle("SET001") } returns AuctionSettleResult("선수1", AuctionOutcome.SOLD)
            every { roomLookupService.get("SET001") } returns room
            every { roomLookupService.getTeamLeaders(room.roomId) } returns emptyList()

            mockMvc.post("/api/v1/rooms/SET001/settle")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.resultType") { value("SUCCESS") }
                    jsonPath("$.success.code") { value("SET001") }
                }
        }

        @Test
        fun `정산할 선수가 없으면 400을 반환한다`() {
            every { auctionService.settle("SET001") } throws IllegalArgumentException("경매할 선수가 없습니다")

            mockMvc.post("/api/v1/rooms/SET001/settle")
                .andExpect {
                    status { isBadRequest() }
                    jsonPath("$.resultType") { value("ERROR") }
                    jsonPath("$.error.errorCode") { value("BAD_REQUEST") }
                }
        }
    }

    @Nested
    inner class `드래프트 픽` {
        @Test
        fun `성공적으로 픽하면 200과 방 정보를 반환한다`() {
            val room = room("PICK01")
            val member =
                RoomTeamMember(
                    roomTeamMemberId = 1L,
                    roomId = 1L,
                    teamLeaderId = "leader-A",
                    playerName = "선수1",
                    assignOrder = 0,
                    createdAt = now,
                    updatedAt = now,
                )
            every { draftService.pick("PICK01", "leader-A", "선수1") } returns member
            every { roomLookupService.get("PICK01") } returns room
            every { roomLookupService.getTeamLeaders(room.roomId) } returns emptyList()

            mockMvc.post("/api/v1/rooms/PICK01/pick") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"teamLeaderId": "leader-A", "playerName": "선수1"}"""
            }.andExpect {
                status { isOk() }
                jsonPath("$.resultType") { value("SUCCESS") }
                jsonPath("$.success.code") { value("PICK01") }
            }
        }

        @Test
        fun `턴이 아닌 팀장이 픽하면 409를 반환한다`() {
            every { draftService.pick("PICK01", "leader-B", "선수1") } throws IllegalStateException("현재 턴이 아닙니다")

            mockMvc.post("/api/v1/rooms/PICK01/pick") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"teamLeaderId": "leader-B", "playerName": "선수1"}"""
            }.andExpect {
                status { isConflict() }
                jsonPath("$.resultType") { value("ERROR") }
                jsonPath("$.error.errorCode") { value("INVALID_STATE") }
            }
        }
    }

    private fun room(
        code: String,
        status: RoomStatus = RoomStatus.WAITING,
    ) = Room(
        roomId = 1L,
        code = code,
        hostId = "host",
        status = status,
        mode = TeamBuildingMode.AUCTION,
        teamCount = 2,
        teamSize = 2,
        budget = 300,
        leaders = listOf(leader(roomId = 1L)),
        createdAt = now,
        updatedAt = now,
    )

    private fun leader(roomId: Long) =
        RoomTeamLeader(
            roomTeamLeaderId = 1L,
            roomId = roomId,
            teamLeaderId = "leader-1",
            nickname = "참가자",
            remainingBudget = 300,
            createdAt = now,
            updatedAt = now,
        )
}
