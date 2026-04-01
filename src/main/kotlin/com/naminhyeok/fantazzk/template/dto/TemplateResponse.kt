package com.naminhyeok.fantazzk.template.dto

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import com.naminhyeok.fantazzk.template.TemplateModel
import com.naminhyeok.fantazzk.template.TemplatePlayerModel
import com.naminhyeok.fantazzk.template.query.TemplateView
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "템플릿 조회 API 의 성공 응답 payload 입니다.")
data class TemplateResponse(
    @field:Schema(description = "템플릿 ID 입니다.", example = "1")
    val id: Long,
    @field:Schema(description = "템플릿 이름입니다.", example = "주말 풋살 경매전")
    val name: String,
    @field:Schema(description = "팀 빌딩 모드입니다.", example = "AUCTION")
    val mode: TeamBuildingMode,
    @field:Schema(description = "팀 수입니다.", example = "2")
    val teamCount: Int,
    @field:Schema(description = "팀별 선수 수입니다.", example = "3")
    val teamSize: Int,
    @field:Schema(description = "AUCTION 모드일 때 사용하는 팀별 예산입니다.", example = "300", nullable = true)
    val budget: Int?,
    @field:Schema(description = "DRAFT 모드일 때 사용하는 픽 순서 전략입니다.", example = "SNAKE", nullable = true)
    val draftOrderStrategy: DraftOrderStrategy?,
    @field:ArraySchema(
        arraySchema = Schema(description = "상세 조회에서는 선수 목록이 포함됩니다. 목록 조회에서는 null 일 수 있습니다.", nullable = true),
        schema = Schema(implementation = TemplatePlayerResponse::class),
    )
    val players: List<TemplatePlayerResponse>?,
) {
    companion object {
        fun from(
            template: TemplateModel,
            players: List<TemplatePlayerModel>? = null,
        ): TemplateResponse =
            TemplateResponse(
                id = template.templateId,
                name = template.name,
                mode = template.mode,
                teamCount = template.teamCount,
                teamSize = template.teamSize,
                budget = template.budget,
                draftOrderStrategy = template.draftOrderStrategy,
                players = players?.map { TemplatePlayerResponse(name = it.name, displayOrder = it.displayOrder) },
            )

        fun from(view: TemplateView): TemplateResponse =
            TemplateResponse(
                id = view.id,
                name = view.name,
                mode = view.mode,
                teamCount = view.teamCount,
                teamSize = view.teamSize,
                budget = view.budget,
                draftOrderStrategy = view.draftOrderStrategy,
                players = view.players?.map { TemplatePlayerResponse(name = it.name, displayOrder = it.displayOrder) },
            )
    }
}
