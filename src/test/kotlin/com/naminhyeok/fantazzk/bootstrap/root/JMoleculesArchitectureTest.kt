package com.naminhyeok.fantazzk.bootstrap.root

import com.naminhyeok.fantazzk.room.application.CreateRoom
import com.naminhyeok.fantazzk.room.repository.RoomRepositoryAdapter
import com.naminhyeok.fantazzk.room.repository.Rooms
import com.naminhyeok.fantazzk.template.application.CreateTemplate
import com.naminhyeok.fantazzk.template.application.FindTemplates
import com.naminhyeok.fantazzk.template.repository.Templates
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.jmolecules.ddd.annotation.Repository as DddRepository
import org.jmolecules.ddd.annotation.Service as DddService
import org.jmolecules.ddd.types.Repository as DddRepositoryType
import org.springframework.stereotype.Repository as SpringRepository

class JMoleculesArchitectureTest {
    @Test
    fun `애플리케이션 서비스는 jMolecules service stereotype 으로 역할을 드러낸다`() {
        val applicationServices =
                listOf(
                    CreateTemplate::class.java,
                    FindTemplates::class.java,
                    CreateRoom::class.java,
                )

        assertThat(applicationServices)
            .allMatch { it.isAnnotationPresent(DddService::class.java) }
    }

    @Test
    fun `리포지토리 계약은 jMolecules repository annotation 과 type 을 함께 사용한다`() {
        val repositoryContracts =
            listOf(
                Templates::class.java,
                Rooms::class.java,
            )

        assertThat(repositoryContracts)
            .allMatch { it.isAnnotationPresent(DddRepository::class.java) }
            .allMatch { DddRepositoryType::class.java.isAssignableFrom(it) }
    }

    @Test
    fun `room repository adapter 는 spring repository stereotype 을 추가하지 않는다`() {
        assertThat(RoomRepositoryAdapter::class.java.isAnnotationPresent(SpringRepository::class.java)).isFalse()
    }
}
