package com.naminhyeok.fantazzk.teambuilding

import com.naminhyeok.fantazzk.teambuilding.dto.CreateTemplateRequest
import com.naminhyeok.fantazzk.teambuilding.dto.TemplateResponse
import com.naminhyeok.fantazzk.teambuilding.template.PlayerEntry
import com.naminhyeok.fantazzk.teambuilding.template.Rules
import com.naminhyeok.fantazzk.teambuilding.template.TemplateId
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/templates")
class TemplateApiController(
    private val templateService: TemplateService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "템플릿 생성", operationId = "createTemplate")
    fun create(
        @RequestBody request: CreateTemplateRequest,
    ): TemplateResponse {
        val rules =
            Rules(
                teamCount = request.teamCount,
                teamSize = request.teamSize,
                budget = request.budget,
                draftOrderStrategy = request.draftOrderStrategy,
            )
        val players = request.players.map { PlayerEntry(it.name, it.metadata) }
        return TemplateResponse.from(templateService.create(request.name, request.mode, rules, players))
    }

    @GetMapping("/{id}")
    @Operation(summary = "템플릿 조회", operationId = "getTemplate")
    fun getById(
        @PathVariable id: Long,
    ): TemplateResponse = TemplateResponse.from(templateService.get(TemplateId(id)))

    @GetMapping
    @Operation(summary = "템플릿 목록 조회", operationId = "listTemplates")
    fun list(): List<TemplateResponse> = templateService.getAll().map(TemplateResponse::from)
}
