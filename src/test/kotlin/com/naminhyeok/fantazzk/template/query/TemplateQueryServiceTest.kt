package com.naminhyeok.fantazzk.template.query

import com.naminhyeok.fantazzk.template.TeamBuildingMode
import com.naminhyeok.fantazzk.template.TemplateIdentity
import com.naminhyeok.fantazzk.template.exception.TemplateException
import com.naminhyeok.fantazzk.template.of
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TemplateQueryServiceTest {
    private lateinit var templateViewRepo: InMemoryTemplateViewProjectionRepository
    private lateinit var templatePlayerViewRepo: InMemoryTemplatePlayerViewProjectionRepository
    private lateinit var cut: TemplateQueryService

    @BeforeEach
    fun setUp() {
        templateViewRepo = InMemoryTemplateViewProjectionRepository()
        templatePlayerViewRepo = InMemoryTemplatePlayerViewProjectionRepository()
        cut = TemplateQueryServiceImpl(templateViewRepo, templatePlayerViewRepo)
    }

    @Test
    fun `상세 조회는 조회 전용 템플릿 모델을 반환한다`() {
        templateViewRepo.save(
            TemplateViewEntity(
                templateId = 1L,
                name = "테스트",
                mode = TeamBuildingMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                draftOrderStrategy = null,
            ),
        )
        templatePlayerViewRepo.save(TemplatePlayerViewEntity(templateId = 1L, name = "선수1", displayOrder = 0))
        templatePlayerViewRepo.save(TemplatePlayerViewEntity(templateId = 1L, name = "선수2", displayOrder = 1))

        val view = cut.getTemplate(TemplateIdentity.of(1L))

        assertThat(view.id).isEqualTo(1L)
        assertThat(view.name).isEqualTo("테스트")
        assertThat(view.players).extracting<String> { it.name }.containsExactly("선수1", "선수2")
    }

    @Test
    fun `목록 조회는 선수 없이 조회 전용 모델 목록을 반환한다`() {
        templateViewRepo.save(
            TemplateViewEntity(
                templateId = 1L,
                name = "첫째",
                mode = TeamBuildingMode.AUCTION,
                teamCount = 2,
                teamSize = 2,
                budget = 300,
                draftOrderStrategy = null,
            ),
        )

        val views = cut.listTemplates()

        val view = views.single()
        assertThat(view.name).isEqualTo("첫째")
        assertThat(view.players).isNull()
    }

    @Test
    fun `존재하지 않는 템플릿은 예외를 던진다`() {
        assertThatThrownBy { cut.getTemplate(TemplateIdentity.of(999L)) }
            .isInstanceOf(TemplateException.TemplateNotFoundException::class.java)
    }

    private class InMemoryTemplateViewProjectionRepository : TemplateViewProjectionRepository {
        private val store = linkedMapOf<Long, TemplateViewEntity>()

        override fun save(entity: TemplateViewEntity): TemplateViewEntity {
            store[entity.templateId] = entity
            return entity
        }

        override fun findById(templateId: Long): TemplateViewEntity? = store[templateId]

        override fun findAll(): List<TemplateViewEntity> = store.values.toList()
    }

    private class InMemoryTemplatePlayerViewProjectionRepository : TemplatePlayerViewProjectionRepository {
        private val store = mutableListOf<TemplatePlayerViewEntity>()
        private var seq = 1L

        override fun save(entity: TemplatePlayerViewEntity): TemplatePlayerViewEntity {
            val saved =
                if (entity.id == 0L) {
                    TemplatePlayerViewEntity(
                        id = seq++,
                        templateId = entity.templateId,
                        name = entity.name,
                        displayOrder = entity.displayOrder,
                    )
                } else {
                    entity
                }
            store.removeIf { it.id == saved.id }
            store.add(saved)
            return saved
        }

        override fun findByTemplateIdOrderByDisplayOrder(templateId: Long): List<TemplatePlayerViewEntity> =
            store.filter { it.templateId == templateId }.sortedBy { it.displayOrder }
    }
}
