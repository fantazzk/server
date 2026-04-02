package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.api.RoomApiController
import com.naminhyeok.fantazzk.room.application.RoomLookupService
import com.naminhyeok.fantazzk.room.repository.RoomBidEntity
import com.naminhyeok.fantazzk.room.repository.RoomBidRepository
import com.naminhyeok.fantazzk.room.repository.RoomEntity
import com.naminhyeok.fantazzk.room.repository.RoomPlayerEntity
import com.naminhyeok.fantazzk.room.repository.RoomPlayerRepository
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderEntity
import com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository
import com.naminhyeok.fantazzk.room.repository.RoomTeamMemberEntity
import com.naminhyeok.fantazzk.room.repository.RoomTeamMemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class RoomStructureTransitionTest {
    @Test
    fun `room 리포지토리는 aggregate 와 RoomId 를 기준으로 동작한다`() {
        assertThat(
            RoomRepository::class.java.getMethod("findById", RoomId::class.java).returnType,
        ).isEqualTo(Room::class.java)
    }

    @Test
    fun `room API 는 더 이상 query service 에 의존하지 않는다`() {
        val parameterTypes = RoomApiController::class.java.declaredConstructors.single().parameterTypes.toList()
        assertThat(parameterTypes.map { it.name })
            .doesNotContain("com.naminhyeok.fantazzk.room.query.RoomQueryService")
    }

    @Test
    fun `room 리포지토리와 조회 서비스는 concrete domain 타입을 사용한다`() {
        assertThat(
            RoomRepository::class.java.getMethod("save", Room::class.java).returnType,
        ).isEqualTo(Room::class.java)
        assertThat(
            RoomRepository::class.java.getMethod("findByCode", String::class.java).returnType,
        ).isEqualTo(Room::class.java)

        assertThat(
            RoomPlayerRepository::class.java.getMethod("save", RoomPlayer::class.java).returnType,
        ).isEqualTo(RoomPlayer::class.java)
        assertThat(
            RoomTeamLeaderRepository::class.java.getMethod("save", RoomTeamLeader::class.java).returnType,
        ).isEqualTo(RoomTeamLeader::class.java)
        assertThat(
            RoomTeamMemberRepository::class.java.getMethod("save", RoomTeamMember::class.java).returnType,
        ).isEqualTo(RoomTeamMember::class.java)
        assertThat(
            RoomBidRepository::class.java.getMethod("save", RoomBid::class.java).returnType,
        ).isEqualTo(RoomBid::class.java)

        assertThat(
            RoomLookupService::class.java.getMethod("get", String::class.java).returnType,
        ).isEqualTo(Room::class.java)
    }

    @Test
    fun `room shim 타입은 더 이상 클래스패스에 존재하지 않는다`() {
        listOf(
            "com.naminhyeok.fantazzk.room.RoomModel",
            "com.naminhyeok.fantazzk.room.RoomProps",
            "com.naminhyeok.fantazzk.room.RoomIdentity",
            "com.naminhyeok.fantazzk.room.RoomPlayerModel",
            "com.naminhyeok.fantazzk.room.RoomPlayerProps",
            "com.naminhyeok.fantazzk.room.RoomPlayerIdentity",
            "com.naminhyeok.fantazzk.room.RoomTeamLeaderModel",
            "com.naminhyeok.fantazzk.room.RoomTeamLeaderProps",
            "com.naminhyeok.fantazzk.room.RoomTeamLeaderIdentity",
            "com.naminhyeok.fantazzk.room.RoomTeamMemberModel",
            "com.naminhyeok.fantazzk.room.RoomTeamMemberProps",
            "com.naminhyeok.fantazzk.room.RoomTeamMemberIdentity",
            "com.naminhyeok.fantazzk.room.RoomBidModel",
            "com.naminhyeok.fantazzk.room.RoomBidProps",
            "com.naminhyeok.fantazzk.room.RoomBidIdentity",
        ).forEach { className ->
            assertThatThrownBy { Class.forName(className) }
                .isInstanceOf(ClassNotFoundException::class.java)
        }

        assertThat(RoomEntity::class.java.interfaces).isEmpty()
        assertThat(RoomPlayerEntity::class.java.interfaces).isEmpty()
        assertThat(RoomTeamLeaderEntity::class.java.interfaces).isEmpty()
        assertThat(RoomTeamMemberEntity::class.java.interfaces).isEmpty()
        assertThat(RoomBidEntity::class.java.interfaces).isEmpty()
    }
}
