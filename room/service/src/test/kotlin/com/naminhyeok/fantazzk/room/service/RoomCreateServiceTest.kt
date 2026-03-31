package com.naminhyeok.fantazzk.room.service

import com.naminhyeok.fantazzk.room.exception.RoomTemplateNotFoundException
import com.naminhyeok.fantazzk.room.infrastructure.RoomRepository
import com.naminhyeok.fantazzk.room.model.DraftOrderStrategy
import com.naminhyeok.fantazzk.room.model.PlayerStatus
import com.naminhyeok.fantazzk.room.model.Room
import com.naminhyeok.fantazzk.room.model.RoomModel
import com.naminhyeok.fantazzk.room.model.RoomProgress
import com.naminhyeok.fantazzk.room.model.RoomStatus
import com.naminhyeok.fantazzk.room.model.TeamBuildingConfiguration
import com.naminhyeok.fantazzk.room.model.TeamBuildingMode
import com.naminhyeok.fantazzk.room.model.configuration
import com.naminhyeok.fantazzk.room.model.progress
import com.naminhyeok.fantazzk.room.service.support.InMemoryRoomPlayerRepository
import com.naminhyeok.fantazzk.room.service.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.service.support.InMemoryRoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.service.support.InMemoryTemplateLookup
import com.naminhyeok.fantazzk.template.api.TemplateDraftStrategy
import com.naminhyeok.fantazzk.template.api.TemplateLookup
import com.naminhyeok.fantazzk.template.api.TemplateLookupException
import com.naminhyeok.fantazzk.template.api.TemplateMode
import com.naminhyeok.fantazzk.template.api.TemplatePlayerView
import com.naminhyeok.fantazzk.template.api.TemplateView
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.springframework.dao.DuplicateKeyException

class RoomCreateServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var playerRepo: InMemoryRoomPlayerRepository
    private lateinit var leaderRepo: InMemoryRoomTeamLeaderRepository
    private lateinit var templateLookup: InMemoryTemplateLookup
    private lateinit var cut: RoomCreateService

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        playerRepo = InMemoryRoomPlayerRepository()
        leaderRepo = InMemoryRoomTeamLeaderRepository()
        templateLookup = InMemoryTemplateLookup()
        cut = RoomCreateServiceImpl(roomRepo, playerRepo, leaderRepo, templateLookup)
    }

    @Nested
    inner class `모드별 필드 전파` {
        @Test
        fun `경매 템플릿으로 방을 생성하면 경매 필드만 채워진다`() {
            templateLookup.addTemplate(
                1L,
                TemplateView(
                    templateId = 1L,
                    mode = TemplateMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    draftOrderStrategy = null,
                    players = listOf(TemplatePlayerView("선수1", 0), TemplatePlayerView("선수2", 1)),
                ),
            )

            val room = cut.create(1L, "호스트")
            val leader = leaderRepo.findByRoomId(room.roomId).single()

            assertThat(room.status).isEqualTo(RoomStatus.WAITING)
            assertThat(room.mode).isEqualTo(TeamBuildingMode.AUCTION)
            assertThat(room.budget).isEqualTo(300)
            assertThat(room.draftOrderStrategy).isNull()
            assertThat(room.currentAuctionRound).isNull()
            assertThat(room.currentTurnIndex).isNull()
            assertThat(Room.from(room).configuration).isEqualTo(
                TeamBuildingConfiguration.Auction(
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )
            assertThat(Room.from(room).progress).isEqualTo(RoomProgress.Waiting)
            assertThat(room.teamCount).isEqualTo(2)
            assertThat(room.teamSize).isEqualTo(2)
            assertThat(leader.teamLeaderId).isEqualTo(room.hostId)
            assertThat(leader.remainingBudget).isEqualTo(300)
        }

        @Test
        fun `드래프트 템플릿으로 방을 생성하면 드래프트 필드만 채워진다`() {
            templateLookup.addTemplate(
                2L,
                TemplateView(
                    templateId = 2L,
                    mode = TemplateMode.DRAFT,
                    teamCount = 2,
                    teamSize = 2,
                    budget = null,
                    draftOrderStrategy = TemplateDraftStrategy.SNAKE,
                    players = listOf(TemplatePlayerView("선수1", 0), TemplatePlayerView("선수2", 1)),
                ),
            )

            val room = cut.create(2L, "호스트")
            val leader = leaderRepo.findByRoomId(room.roomId).single()

            assertThat(room.mode).isEqualTo(TeamBuildingMode.DRAFT)
            assertThat(room.budget).isNull()
            assertThat(room.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
            assertThat(room.currentAuctionRound).isNull()
            assertThat(room.currentTurnIndex).isNull()
            assertThat(Room.from(room).configuration).isEqualTo(
                TeamBuildingConfiguration.Draft(
                    teamCount = 2,
                    teamSize = 2,
                    strategy = DraftOrderStrategy.SNAKE,
                ),
            )
            assertThat(Room.from(room).progress).isEqualTo(RoomProgress.Waiting)
            assertThat(leader.remainingBudget).isNull()
        }
    }

    @Nested
    inner class `생성 시 초기화 계약` {
        @RepeatedTest(10)
        fun `방 생성 시 6자리 영대문자와 숫자 코드가 발급된다`() {
            addAuctionTemplate(templateId = 1L)

            val room = cut.create(1L, "호스트")

            assertThat(room.code).hasSize(6)
            assertThat(room.code).matches("[A-Z0-9]{6}")
        }

        @Test
        fun `방 생성 시 호스트가 첫 번째 팀장으로 등록된다`() {
            addAuctionTemplate(templateId = 1L)

            val room = cut.create(1L, "호스트닉네임")

            val leaders = leaderRepo.findByRoomId(room.roomId)
            assertThat(leaders).hasSize(1)
            assertThat(leaders.first().nickname).isEqualTo("호스트닉네임")
            assertThat(leaders.first().teamLeaderId).isEqualTo(room.hostId)
        }

        @Test
        fun `방 생성 시 템플릿 선수 목록이 표시 순서와 상태를 유지한 채 복사된다`() {
            templateLookup.addTemplate(
                1L,
                TemplateView(
                    templateId = 1L,
                    mode = TemplateMode.AUCTION,
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                    draftOrderStrategy = null,
                    players =
                        listOf(
                            TemplatePlayerView("선수A", 2),
                            TemplatePlayerView("선수B", 0),
                            TemplatePlayerView("선수C", 1),
                        ),
                ),
            )

            val room = cut.create(1L, "호스트")
            val roomPlayers = playerRepo.findByRoomId(room.roomId)

            assertThat(roomPlayers).hasSize(3)
            assertThat(roomPlayers.map { Triple(it.name, it.displayOrder, it.status) })
                .containsExactly(
                    Triple("선수B", 0, PlayerStatus.AVAILABLE),
                    Triple("선수C", 1, PlayerStatus.AVAILABLE),
                    Triple("선수A", 2, PlayerStatus.AVAILABLE),
                )
        }
    }

    @Nested
    inner class `템플릿 조회 실패` {
        @Test
        fun `포트에서 템플릿 없음 예외가 오면 방 도메인 예외로 번역한다`() {
            cut =
                RoomCreateServiceImpl(
                    roomRepo,
                    playerRepo,
                    leaderRepo,
                    object : TemplateLookup {
                        override fun get(templateId: Long): TemplateView {
                            throw TemplateLookupException.NotFound(templateId)
                        }
                    },
                )

            assertThatThrownBy { cut.create(999L, "호스트") }
                .isInstanceOf(RoomTemplateNotFoundException::class.java)
                .hasMessage("템플릿을 찾을 수 없습니다")
        }

        @Test
        fun `포트에서 invalid 템플릿 예외가 오면 생성 불가 상태로 번역한다`() {
            cut =
                RoomCreateServiceImpl(
                    roomRepo,
                    playerRepo,
                    leaderRepo,
                    object : TemplateLookup {
                        override fun get(templateId: Long): TemplateView {
                            throw TemplateLookupException.Invalid(templateId)
                        }
                    },
                )

            assertThatThrownBy { cut.create(999L, "호스트") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("유효하지 않은 템플릿입니다")
        }
    }

    @Nested
    inner class `코드 발급 재시도` {
        @Test
        fun `저장 시 코드 중복이 발생하면 다른 코드로 재시도한다`() {
            val retryingRoomRepo = DuplicateOnceWithDistinctRetryRoomRepository()
            addAuctionTemplate(templateId = 1L)
            cut = RoomCreateServiceImpl(retryingRoomRepo, playerRepo, leaderRepo, templateLookup)

            val room = cut.create(1L, "호스트")

            assertThat(room.roomId).isPositive()
            assertThat(retryingRoomRepo.saveAttempts).isEqualTo(2)
            assertThat(retryingRoomRepo.attemptedCodes).hasSize(2)
            assertThat(retryingRoomRepo.attemptedCodes.distinct()).hasSize(2)
            assertThat(room.code).isNotEqualTo(retryingRoomRepo.collidingCode)
        }

        @Test
        fun `조회 단계에서 계속 충돌하는 코드만 생성되면 최대 횟수까지만 재시도한다`() {
            val alwaysExistingCodeRoomRepository = AlwaysExistingCodeRoomRepository()
            addAuctionTemplate(templateId = 1L)
            cut = RoomCreateServiceImpl(alwaysExistingCodeRoomRepository, playerRepo, leaderRepo, templateLookup)

            assertThatThrownBy { cut.create(1L, "호스트") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("방 코드를 생성할 수 없습니다")
            assertThat(alwaysExistingCodeRoomRepository.findByCodeAttempts).isEqualTo(5)
            assertThat(alwaysExistingCodeRoomRepository.saveAttempts).isZero()
        }

        @Test
        fun `저장 시 코드 중복이 계속 발생하면 최대 횟수 이후 생성에 실패한다`() {
            val alwaysDuplicateRoomRepo = AlwaysDuplicateRoomRepository()
            addAuctionTemplate(templateId = 1L)
            cut = RoomCreateServiceImpl(alwaysDuplicateRoomRepo, playerRepo, leaderRepo, templateLookup)

            assertThatThrownBy { cut.create(1L, "호스트") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("방 코드를 생성할 수 없습니다")
            assertThat(alwaysDuplicateRoomRepo.saveAttempts).isEqualTo(5)
        }
    }

    private fun addAuctionTemplate(templateId: Long) {
        templateLookup.addTemplate(
            templateId,
            TemplateView(
                templateId = templateId,
                mode = TemplateMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                draftOrderStrategy = null,
                players = emptyList(),
            ),
        )
    }

    private class DuplicateOnceWithDistinctRetryRoomRepository : RoomRepository {
        private val delegate = InMemoryRoomRepository()
        var saveAttempts: Int = 0
        val attemptedCodes = mutableListOf<String>()
        var collidingCode: String? = null

        override fun save(room: Room): RoomModel {
            saveAttempts += 1
            attemptedCodes += room.code
            if (saveAttempts == 1) {
                collidingCode = room.code
                throw DuplicateKeyException("duplicate room code")
            }
            check(room.code != collidingCode) { "중복된 방 코드를 그대로 재사용했습니다" }
            return delegate.save(room)
        }

        override fun findByCode(code: String): RoomModel? = delegate.findByCode(code)

        override fun findById(roomId: Long): RoomModel? = delegate.findById(roomId)
    }

    private class AlwaysDuplicateRoomRepository : RoomRepository {
        var saveAttempts: Int = 0

        override fun save(room: Room): RoomModel {
            saveAttempts += 1
            throw DuplicateKeyException("duplicate room code")
        }

        override fun findByCode(code: String): RoomModel? = null

        override fun findById(roomId: Long): RoomModel? = null
    }

    private class AlwaysExistingCodeRoomRepository : RoomRepository {
        var findByCodeAttempts: Int = 0
        var saveAttempts: Int = 0

        override fun save(room: Room): RoomModel {
            saveAttempts += 1
            return room.copy(roomId = 1L)
        }

        override fun findByCode(code: String): RoomModel {
            findByCodeAttempts += 1
            return Room(
                roomId = 1L,
                code = code,
                hostId = "existing-host",
                status = RoomStatus.WAITING,
                mode = TeamBuildingMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
            )
        }

        override fun findById(roomId: Long): RoomModel? = null
    }
}
