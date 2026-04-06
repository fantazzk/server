@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.CreateRoom
import com.naminhyeok.fantazzk.room.application.RoomCreateAttemptExecutor
import com.naminhyeok.fantazzk.room.domain.*
import com.naminhyeok.fantazzk.room.exception.RoomTemplateNotFoundException
import com.naminhyeok.fantazzk.room.repository.Rooms
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.support.InMemoryTemplateCatalog
import com.naminhyeok.fantazzk.template.TemplateBlueprint
import com.naminhyeok.fantazzk.template.TemplateCatalog
import com.naminhyeok.fantazzk.template.TemplateCatalogException
import com.naminhyeok.fantazzk.template.TemplateDraftOrderStrategy
import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.TemplateMode
import com.naminhyeok.fantazzk.template.TemplatePlayerBlueprint
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException

class RoomCreateServiceTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var templateCatalog: InMemoryTemplateCatalog
    private lateinit var roomCreateAttemptExecutor: RoomCreateAttemptExecutor
    private lateinit var cut: CreateRoom

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        templateCatalog = InMemoryTemplateCatalog()
        roomCreateAttemptExecutor = RoomCreateAttemptExecutor(roomRepo)
        cut = CreateRoom(roomRepo, templateCatalog, roomCreateAttemptExecutor)
    }

    @Nested
    inner class `템플릿 계약 번역` {
        @Test
        fun `경매 템플릿으로 방을 생성하면 경매 필드만 채워진다`() {
            templateCatalog.addTemplate(
                templateId(1),
                TemplateBlueprint(
                    templateId(1),
                    TemplateMode.AUCTION,
                    2,
                    2,
                    300,
                    null,
                    listOf(TemplatePlayerBlueprint("선수1", 0), TemplatePlayerBlueprint("선수2", 1)),
                ),
            )

            val room = cut.create(templateId(1), "호스트")
            val leader = room.leaders.single()

            assertThat(room.status).isEqualTo(RoomStatus.WAITING)
            assertThat(room.mode).isEqualTo(TeamBuildingMode.AUCTION)
            assertThat(room.budget).isEqualTo(300)
            assertThat(room.draftOrderStrategy).isNull()
            assertThat(room.currentAuctionRound).isNull()
            assertThat(room.currentTurnIndex).isNull()
            assertThat(room.configuration).isEqualTo(
                TeamBuildingConfiguration.Auction(
                    teamCount = 2,
                    teamSize = 2,
                    budget = 300,
                ),
            )
            assertThat(room.progress).isEqualTo(RoomProgress.Waiting)
            assertThat(room.teamCount).isEqualTo(2)
            assertThat(room.teamSize).isEqualTo(2)
            assertThat(leader.teamLeaderId).isEqualTo(room.hostId)
            assertThat(leader.remainingBudget).isEqualTo(300)
        }

        @Test
        fun `드래프트 템플릿으로 방을 생성하면 드래프트 필드만 채워진다`() {
            templateCatalog.addTemplate(
                templateId(2),
                TemplateBlueprint(
                    templateId(2),
                    TemplateMode.DRAFT,
                    2,
                    2,
                    null,
                    TemplateDraftOrderStrategy.SNAKE,
                    listOf(TemplatePlayerBlueprint("선수1", 0), TemplatePlayerBlueprint("선수2", 1)),
                ),
            )

            val room = cut.create(templateId(2), "호스트")
            val leader = room.leaders.single()

            assertThat(room.mode).isEqualTo(TeamBuildingMode.DRAFT)
            assertThat(room.budget).isNull()
            assertThat(room.draftOrderStrategy).isEqualTo(DraftOrderStrategy.SNAKE)
            assertThat(room.currentAuctionRound).isNull()
            assertThat(room.currentTurnIndex).isNull()
            assertThat(room.configuration).isEqualTo(
                TeamBuildingConfiguration.Draft(
                    teamCount = 2,
                    teamSize = 2,
                    strategy = DraftOrderStrategy.SNAKE,
                ),
            )
            assertThat(room.progress).isEqualTo(RoomProgress.Waiting)
            assertThat(leader.remainingBudget).isNull()
        }
    }

    @Nested
    inner class `생성 시 초기화 계약` {
        @RepeatedTest(10)
        fun `방 생성 시 6자리 영대문자와 숫자 코드가 발급된다`() {
            addAuctionTemplate(templateId(1))

            val room = cut.create(templateId(1), "호스트")

            assertThat(room.code).hasSize(6)
            assertThat(room.code).matches("[A-Z0-9]{6}")
        }

        @Test
        fun `방 생성 시 호스트가 첫 번째 팀장으로 등록된다`() {
            addAuctionTemplate(templateId(1))

            val room = cut.create(templateId(1), "호스트닉네임")

            val leaders = room.leaders
            assertThat(leaders).hasSize(1)
            assertThat(leaders.first().nickname).isEqualTo("호스트닉네임")
            assertThat(leaders.first().teamLeaderId).isEqualTo(room.hostId)
        }

        @Test
        fun `방 생성 시 템플릿 선수 목록이 표시 순서와 상태를 유지한 채 복사된다`() {
            templateCatalog.addTemplate(
                templateId(1),
                TemplateBlueprint(
                    templateId(1),
                    TemplateMode.AUCTION,
                    2,
                    2,
                    300,
                    null,
                    listOf(
                        TemplatePlayerBlueprint("선수A", 2),
                        TemplatePlayerBlueprint("선수B", 0),
                        TemplatePlayerBlueprint("선수C", 1),
                    ),
                ),
            )

            val room = cut.create(templateId(1), "호스트")
            val roomPlayers = room.players

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
                CreateRoom(
                    roomRepo,
                    object : TemplateCatalog {
                        override fun getTemplateBlueprint(templateId: TemplateId): TemplateBlueprint {
                            throw TemplateCatalogException.NotFound(templateId)
                        }
                    },
                    roomCreateAttemptExecutor,
                )

            assertThatThrownBy { cut.create(templateId(999), "호스트") }
                .isInstanceOf(RoomTemplateNotFoundException::class.java)
                .hasMessage("템플릿을 찾을 수 없습니다")
        }

        @Test
        fun `포트에서 유효하지 않은 템플릿 예외가 오면 생성 불가 상태로 번역한다`() {
            cut =
                CreateRoom(
                    roomRepo,
                    object : TemplateCatalog {
                        override fun getTemplateBlueprint(templateId: TemplateId): TemplateBlueprint {
                            throw TemplateCatalogException.Invalid(templateId)
                        }
                    },
                    roomCreateAttemptExecutor,
                )

            assertThatThrownBy { cut.create(templateId(999), "호스트") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("유효하지 않은 템플릿입니다")
        }
    }

    @Nested
    inner class `코드 발급 재시도` {
        @Test
        fun `저장 시 코드 중복이 발생하면 다른 코드로 재시도한다`() {
            val retryingRoomRepo = DuplicateOnceWithDistinctRetryRoomRepository()
            addAuctionTemplate(templateId(1))
            cut =
                CreateRoom(
                    retryingRoomRepo,
                    templateCatalog,
                    RoomCreateAttemptExecutor(retryingRoomRepo),
                )

            val room = cut.create(templateId(1), "호스트")

            assertThat(room.roomId).isPositive()
            assertThat(retryingRoomRepo.saveAttempts).isEqualTo(2)
            assertThat(retryingRoomRepo.attemptedCodes).hasSize(2)
            assertThat(retryingRoomRepo.attemptedCodes.distinct()).hasSize(2)
            assertThat(room.code).isNotEqualTo(retryingRoomRepo.collidingCode)
        }

        @Test
        fun `JPA 저장소에서 무결성 예외가 발생해도 다른 코드로 재시도한다`() {
            val retryingRoomRepo = DuplicateOnceWithDataIntegrityRetryRoomRepository()
            addAuctionTemplate(templateId(1))
            cut =
                CreateRoom(
                    retryingRoomRepo,
                    templateCatalog,
                    RoomCreateAttemptExecutor(retryingRoomRepo),
                )

            val room = cut.create(templateId(1), "호스트")

            assertThat(room.roomId).isPositive()
            assertThat(retryingRoomRepo.saveAttempts).isEqualTo(2)
            assertThat(retryingRoomRepo.attemptedCodes).hasSize(2)
            assertThat(retryingRoomRepo.attemptedCodes.distinct()).hasSize(2)
            assertThat(room.code).isNotEqualTo(retryingRoomRepo.collidingCode)
        }

        @Test
        fun `조회 단계에서 계속 충돌하는 코드만 생성되면 최대 횟수까지만 재시도한다`() {
            val alwaysExistingCodeRoomRepository = AlwaysExistingCodeRoomRepository()
            addAuctionTemplate(templateId(1))
            cut =
                CreateRoom(
                    alwaysExistingCodeRoomRepository,
                    templateCatalog,
                    RoomCreateAttemptExecutor(alwaysExistingCodeRoomRepository),
                )

            assertThatThrownBy { cut.create(templateId(1), "호스트") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("방 코드를 생성할 수 없습니다")
            assertThat(alwaysExistingCodeRoomRepository.findByCodeAttempts).isEqualTo(5)
            assertThat(alwaysExistingCodeRoomRepository.saveAttempts).isZero()
        }

        @Test
        fun `저장 시 코드 중복이 계속 발생하면 최대 횟수 이후 생성에 실패한다`() {
            val alwaysDuplicateRoomRepo = AlwaysDuplicateRoomRepository()
            addAuctionTemplate(templateId(1))
            cut =
                CreateRoom(
                    alwaysDuplicateRoomRepo,
                    templateCatalog,
                    RoomCreateAttemptExecutor(alwaysDuplicateRoomRepo),
                )

            assertThatThrownBy { cut.create(templateId(1), "호스트") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("방 코드를 생성할 수 없습니다")
            assertThat(alwaysDuplicateRoomRepo.saveAttempts).isEqualTo(5)
        }
    }

    private fun addAuctionTemplate(templateId: TemplateId) {
        templateCatalog.addTemplate(
            templateId,
            TemplateBlueprint(
                templateId,
                TemplateMode.AUCTION,
                2,
                2,
                300,
                null,
                emptyList(),
            ),
        )
    }

    private fun templateId(number: Long): TemplateId = TemplateId.from(templateIdText(number))

    private fun templateIdText(number: Long): String = "00000000-0000-0000-0000-${number.toString().padStart(12, '0')}"

    private class DuplicateOnceWithDistinctRetryRoomRepository : Rooms {
        private val delegate = InMemoryRoomRepository()
        var saveAttempts: Int = 0
        val attemptedCodes = mutableListOf<String>()
        var collidingCode: String? = null

        override fun save(room: Room): Room {
            saveAttempts += 1
            attemptedCodes += room.code
            if (saveAttempts == 1) {
                collidingCode = room.code
                throw DuplicateKeyException("duplicate room code")
            }
            check(room.code != collidingCode) { "중복된 방 코드를 그대로 재사용했습니다" }
            return delegate.save(room)
        }

        override fun findByCode(code: String): Room? = delegate.findByCode(code)

        override fun findById(roomId: RoomId): Room? = delegate.findById(roomId)
    }

    private class AlwaysDuplicateRoomRepository : Rooms {
        var saveAttempts: Int = 0

        override fun save(room: Room): Room {
            saveAttempts += 1
            throw DuplicateKeyException("duplicate room code")
        }

        override fun findByCode(code: String): Room? = null

        override fun findById(roomId: RoomId): Room? = null
    }

    private class DuplicateOnceWithDataIntegrityRetryRoomRepository : Rooms {
        private val delegate = InMemoryRoomRepository()
        var saveAttempts: Int = 0
        val attemptedCodes = mutableListOf<String>()
        var collidingCode: String? = null
        private var collisionVisible = false

        override fun save(room: Room): Room {
            saveAttempts += 1
            attemptedCodes += room.code
            if (saveAttempts == 1) {
                collidingCode = room.code
                collisionVisible = true
                throw DataIntegrityViolationException("duplicate room code")
            }
            check(room.code != collidingCode) { "중복된 방 코드를 그대로 재사용했습니다" }
            return delegate.save(room)
        }

        override fun findByCode(code: String): Room? =
            when {
                collisionVisible && code == collidingCode ->
                    Room(
                        roomId = 99L,
                        code = code,
                        hostId = "existing",
                        status = RoomStatus.WAITING,
                        mode = TeamBuildingMode.AUCTION,
                        teamCount = 2,
                        teamSize = 2,
                        budget = 300,
                    )
                else -> delegate.findByCode(code)
            }

        override fun findById(roomId: RoomId): Room? = delegate.findById(roomId)
    }

    private class AlwaysExistingCodeRoomRepository : Rooms {
        var findByCodeAttempts: Int = 0
        var saveAttempts: Int = 0

        override fun save(room: Room): Room {
            saveAttempts += 1
            return room.copy(roomId = 1L)
        }

        override fun findByCode(code: String): Room {
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

        override fun findById(roomId: RoomId): Room? = null
    }
}
