package com.naminhyeok.fantazzk.room.query

import com.naminhyeok.fantazzk.room.Room
import com.naminhyeok.fantazzk.room.RoomStatus
import com.naminhyeok.fantazzk.room.TeamBuildingMode
import com.naminhyeok.fantazzk.room.exception.RoomException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomQueryServiceTest {
    private lateinit var roomViewRepo: InMemoryRoomViewProjectionRepository
    private lateinit var teamLeaderViewRepo: InMemoryTeamLeaderViewProjectionRepository
    private lateinit var cut: RoomQueryService

    @BeforeEach
    fun setUp() {
        roomViewRepo = InMemoryRoomViewProjectionRepository()
        teamLeaderViewRepo = InMemoryTeamLeaderViewProjectionRepository()
        cut = RoomQueryServiceImpl(roomViewRepo, teamLeaderViewRepo)
    }

    @Test
    fun `코드로 조회하면 방 조회 전용 모델을 반환한다`() {
        val room =
            Room(
                code = "LOOK01",
                hostId = "host",
                status = RoomStatus.WAITING,
                mode = TeamBuildingMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
            )
        roomViewRepo.save(RoomViewEntity(roomId = room.roomId, code = room.code, status = room.status))
        teamLeaderViewRepo.save(
            TeamLeaderViewEntity(
                roomId = room.roomId,
                teamLeaderId = "leader-1",
                nickname = "참가자",
                remainingBudget = 300,
            ),
        )

        val view = cut.getRoom("LOOK01")

        assertThat(view.code).isEqualTo("LOOK01")
        assertThat(view.status).isEqualTo(RoomStatus.WAITING)
        val leader = view.teamLeaders.single()
        assertThat(leader.id).isEqualTo("leader-1")
        assertThat(leader.nickname).isEqualTo("참가자")
        assertThat(leader.remainingBudget).isEqualTo(300)
    }

    @Test
    fun `존재하지 않는 코드로 조회하면 방 예외를 던진다`() {
        assertThatThrownBy { cut.getRoom("NOCODE") }
            .isInstanceOf(RoomException.RoomNotFoundException::class.java)
    }

    private class InMemoryRoomViewProjectionRepository : RoomViewProjectionRepository {
        private val store = linkedMapOf<Long, RoomViewEntity>()

        override fun save(entity: RoomViewEntity): RoomViewEntity {
            store[entity.roomId] = entity
            return entity
        }

        override fun findByCode(code: String): RoomViewEntity? = store.values.firstOrNull { it.code == code }

        override fun findAll(): List<RoomViewEntity> = store.values.toList()
    }

    private class InMemoryTeamLeaderViewProjectionRepository : TeamLeaderViewProjectionRepository {
        private val store = linkedMapOf<Pair<Long, String>, TeamLeaderViewEntity>()
        private var seq = 1L

        override fun save(entity: TeamLeaderViewEntity): TeamLeaderViewEntity {
            val saved =
                if (entity.id == 0L) {
                    TeamLeaderViewEntity(
                        id = seq++,
                        roomId = entity.roomId,
                        teamLeaderId = entity.teamLeaderId,
                        nickname = entity.nickname,
                        remainingBudget = entity.remainingBudget,
                    )
                } else {
                    entity
                }
            store[saved.roomId to saved.teamLeaderId] = saved
            return saved
        }

        override fun findByRoomIdOrderById(roomId: Long): List<TeamLeaderViewEntity> =
            store.values.filter { it.roomId == roomId }.sortedBy { it.id }

        override fun findByRoomIdAndTeamLeaderId(
            roomId: Long,
            teamLeaderId: String,
        ): TeamLeaderViewEntity? = store[roomId to teamLeaderId]
    }
}
