@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.application.GetRoom
import com.naminhyeok.fantazzk.room.domain.*
import com.naminhyeok.fantazzk.room.exception.RoomException
import com.naminhyeok.fantazzk.room.support.InMemoryRoomRepository
import com.naminhyeok.fantazzk.room.support.roomFixture
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoomFinderTest {
    private lateinit var roomRepo: InMemoryRoomRepository
    private lateinit var cut: GetRoom

    @BeforeEach
    fun setUp() {
        roomRepo = InMemoryRoomRepository()
        cut = GetRoom(roomRepo)
    }

    @Test
    fun `코드로 방 aggregate 를 조회한다`() {
        roomRepo.save(
            roomFixture(
                code = "LOOK01",
                hostId = "host",
                status = RoomStatus.WAITING,
                mode = TeamBuildingMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
            ),
        )

        val room = cut.get("LOOK01")

        assertThat(room.code).isEqualTo("LOOK01")
        assertThat(room.status).isEqualTo(RoomStatus.WAITING)
    }

    @Test
    fun `존재하지 않는 코드로 조회하면 예외가 발생한다`() {
        assertThatThrownBy { cut.get("NOCODE") }
            .isInstanceOf(RoomException.RoomNotFoundException::class.java)
    }
}
