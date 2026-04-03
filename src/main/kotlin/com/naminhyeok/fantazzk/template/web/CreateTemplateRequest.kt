package com.naminhyeok.fantazzk.template.web

import com.naminhyeok.fantazzk.template.DraftOrderStrategy
import com.naminhyeok.fantazzk.template.TeamBuildingMode
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "템플릿 생성 요청입니다.")
data class CreateTemplateRequest(
    @field:Schema(description = "템플릿 이름입니다.", example = "주말 풋살 경매전")
    val name: String,
    @field:Schema(description = "팀 빌딩 모드입니다.", example = "AUCTION")
    val mode: TeamBuildingMode,
    @field:Schema(description = "팀 수입니다.", example = "2")
    val teamCount: Int,
    @field:Schema(description = "팀별 선수 수입니다.", example = "3")
    val teamSize: Int,
    @field:Schema(description = "AUCTION 모드일 때 사용할 팀별 예산입니다. DRAFT 모드에서는 null 입니다.", example = "300", nullable = true)
    val budget: Int? = null,
    @field:Schema(description = "DRAFT 모드일 때 사용할 픽 순서 전략입니다. AUCTION 모드에서는 null 입니다.", example = "SNAKE", nullable = true)
    val draftOrderStrategy: DraftOrderStrategy? = null,
    @field:ArraySchema(
        arraySchema = Schema(description = "방 생성 시 복제될 선수 이름 목록입니다. 입력 순서가 displayOrder 가 됩니다."),
        schema = Schema(example = "김민수"),
    )
    val playerNames: List<String>,
)
