package com.naminhyeok.fantazzk.room

import com.naminhyeok.fantazzk.room.api.RoomApiController
import com.naminhyeok.fantazzk.room.application.AuctionServiceImpl
import com.naminhyeok.fantazzk.room.application.DraftServiceImpl
import com.naminhyeok.fantazzk.room.application.RoomCreateAttemptExecutorImpl
import com.naminhyeok.fantazzk.room.application.RoomCreateServiceImpl
import com.naminhyeok.fantazzk.room.application.RoomFinder
import com.naminhyeok.fantazzk.room.application.RoomFinderImpl
import com.naminhyeok.fantazzk.room.application.RoomJoinServiceImpl
import com.naminhyeok.fantazzk.room.application.RoomStartServiceImpl
import com.naminhyeok.fantazzk.room.repository.RoomRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

class RoomStructureTransitionTest {
    @Test
    fun `room 리포지토리 공개 surface 는 typed aggregate 계약만 노출한다`() {
        val methods = RoomRepository::class.java.methods.filter { it.declaringClass == RoomRepository::class.java }

        assertThat(RoomRepository::class.java.interfaces.map { it.simpleName }).doesNotContain("JpaRepository")
        assertThat(methods.map { it.name to it.parameterTypes.toList() })
            .contains("save" to listOf(Room::class.java))
            .contains("findByCode" to listOf(String::class.java))
            .contains("findById" to listOf(RoomId::class.java))
    }

    @Test
    fun `room API 는 더 이상 query service 에 의존하지 않는다`() {
        val parameterTypes = RoomApiController::class.java.declaredConstructors.single().parameterTypes.toList()
        assertThat(parameterTypes.map { it.name })
            .doesNotContain("com.naminhyeok.fantazzk.room.query.RoomQueryService")
            .doesNotContain("com.naminhyeok.fantazzk.room.application.RoomLookupService")
    }

    @Test
    fun `room finder 와 유스케이스 서비스는 단일 aggregate 리포지토리에만 의존한다`() {
        val createDependencies = RoomCreateServiceImpl::class.java.declaredConstructors.single().parameterTypes.map { it.simpleName }
        val finderDependencies = RoomFinderImpl::class.java.declaredConstructors.single().parameterTypes.map { it.simpleName }
        val joinDependencies = RoomJoinServiceImpl::class.java.declaredConstructors.single().parameterTypes.map { it.simpleName }
        val startDependencies = RoomStartServiceImpl::class.java.declaredConstructors.single().parameterTypes.map { it.simpleName }
        val draftDependencies = DraftServiceImpl::class.java.declaredConstructors.single().parameterTypes.map { it.simpleName }
        val auctionDependencies = AuctionServiceImpl::class.java.declaredConstructors.single().parameterTypes.map { it.simpleName }

        listOf(
            createDependencies,
            finderDependencies,
            joinDependencies,
            startDependencies,
            draftDependencies,
            auctionDependencies,
        ).forEach { dependencies ->
            assertThat(dependencies).doesNotContain(
                "RoomPlayerRepository",
                "RoomTeamLeaderRepository",
                "RoomTeamMemberRepository",
                "RoomBidRepository",
            )
            assertThat(dependencies).contains("RoomRepository")
        }
    }

    @Test
    fun `방 생성 재시도는 외부 서비스와 개별 시도 트랜잭션으로 분리한다`() {
        val createMethod = RoomCreateServiceImpl::class.java.getMethod("create", Long::class.javaPrimitiveType, String::class.java)
        val attemptMethod = RoomCreateAttemptExecutorImpl::class.java.getMethod("create", Room::class.java)

        assertThat(createMethod.isAnnotationPresent(Transactional::class.java)).isFalse()
        assertThat(attemptMethod.getAnnotation(Transactional::class.java).propagation).isEqualTo(Propagation.REQUIRES_NEW)
    }

    @Test
    fun `room main 코드에는 더 이상 split child repository surface 가 존재하지 않는다`() {
        listOf(
            "com.naminhyeok.fantazzk.room.repository.RoomPlayerRepository",
            "com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderRepository",
            "com.naminhyeok.fantazzk.room.repository.RoomTeamMemberRepository",
            "com.naminhyeok.fantazzk.room.repository.RoomBidRepository",
            "com.naminhyeok.fantazzk.room.repository.RoomPlayerJdbcRepository",
            "com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderJdbcRepository",
            "com.naminhyeok.fantazzk.room.repository.RoomTeamMemberJdbcRepository",
            "com.naminhyeok.fantazzk.room.repository.RoomBidJdbcRepository",
            "com.naminhyeok.fantazzk.room.repository.RoomRepositoryConfiguration",
        ).forEach { className ->
            assertThatThrownBy { Class.forName(className) }
                .isInstanceOf(ClassNotFoundException::class.java)
        }
    }

    @Test
    fun `room 메인 코드에는 더 이상 jdbc 설정과 JDBC 전용 엔티티가 존재하지 않는다`() {
        listOf(
            "com.naminhyeok.fantazzk.RootCombinedJdbcConfiguration",
            "com.naminhyeok.fantazzk.room.config.RoomJdbcConfiguration",
            "com.naminhyeok.fantazzk.room.config.EnumConverters",
            "com.naminhyeok.fantazzk.room.repository.RoomEntity",
            "com.naminhyeok.fantazzk.room.repository.RoomPlayerEntity",
            "com.naminhyeok.fantazzk.room.repository.RoomTeamLeaderEntity",
            "com.naminhyeok.fantazzk.room.repository.RoomTeamMemberEntity",
            "com.naminhyeok.fantazzk.room.repository.RoomBidEntity",
            "com.naminhyeok.fantazzk.room.repository.RoomIdAttributeConverter",
        ).forEach { className ->
            assertThatThrownBy { Class.forName(className) }
                .isInstanceOf(ClassNotFoundException::class.java)
        }
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
            RoomFinder::class.java.getMethod("get", String::class.java).returnType,
        ).isEqualTo(Room::class.java)
    }

    @Test
    fun `room query projection 타입은 더 이상 클래스패스에 존재하지 않는다`() {
        listOf(
            "com.naminhyeok.fantazzk.room.query.RoomQueryService",
            "com.naminhyeok.fantazzk.room.query.RoomProjectionUpdater",
            "com.naminhyeok.fantazzk.room.query.RoomProjectionWriter",
            "com.naminhyeok.fantazzk.room.query.RoomViewProjectionRepository",
            "com.naminhyeok.fantazzk.room.query.TeamLeaderViewProjectionRepository",
            "com.naminhyeok.fantazzk.room.query.RoomView",
            "com.naminhyeok.fantazzk.room.query.TeamLeaderView",
        ).forEach { className ->
            assertThatThrownBy { Class.forName(className) }
                .isInstanceOf(ClassNotFoundException::class.java)
        }
    }

    @Test
    fun `room aggregate 생성 명세는 room 루트 패키지에 둔다`() {
        assertThat(Class.forName("com.naminhyeok.fantazzk.room.RoomTemplateSpec")).isNotNull
        assertThatThrownBy { Class.forName("com.naminhyeok.fantazzk.room.application.RoomTemplateSpec") }
            .isInstanceOf(ClassNotFoundException::class.java)
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
    }
}
