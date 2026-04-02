package com.naminhyeok.fantazzk.room

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
import org.junit.jupiter.api.Test

class RoomStructureTransitionTest {
    @Test
    fun `room 리포지토리와 조회 서비스는 concrete domain 타입을 사용한다`() {
        assertThat(
            RoomRepository::class.java.getMethod("save", Room::class.java).returnType,
        ).isEqualTo(Room::class.java)
        assertThat(
            RoomRepository::class.java.getMethod("findByCode", String::class.java).returnType,
        ).isEqualTo(Room::class.java)
        assertThat(
            RoomRepository::class.java.getMethod("findById", Long::class.java).returnType,
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
    fun `room persistence entity 는 model 인터페이스를 구현하지 않는다`() {
        assertThat(RoomEntity::class.java.interfaces).doesNotContain(RoomModel::class.java)
        assertThat(RoomPlayerEntity::class.java.interfaces).doesNotContain(RoomPlayerModel::class.java)
        assertThat(RoomTeamLeaderEntity::class.java.interfaces).doesNotContain(RoomTeamLeaderModel::class.java)
        assertThat(RoomTeamMemberEntity::class.java.interfaces).doesNotContain(RoomTeamMemberModel::class.java)
        assertThat(RoomBidEntity::class.java.interfaces).doesNotContain(RoomBidModel::class.java)
    }
}
