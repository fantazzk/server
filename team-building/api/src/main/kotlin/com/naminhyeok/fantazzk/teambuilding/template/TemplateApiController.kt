package com.naminhyeok.fantazzk.teambuilding.template

import com.naminhyeok.fantazzk.teambuilding.dto.CreateTemplateRequest
import com.naminhyeok.fantazzk.teambuilding.dto.TemplateResponse
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
    private val templateCreateService: TemplateCreateService,
    private val templateLookUpService: TemplateLookUpService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "템플릿 생성", operationId = "createTemplate")
    fun create(
        @RequestBody request: CreateTemplateRequest,
    ): TemplateResponse =
        TemplateResponse.from(
            templateCreateService.create(
                name = request.name,
                mode = request.mode,
                teamCount = request.teamCount,
                teamSize = request.teamSize,
                budget = request.budget,
                draftOrderStrategy = request.draftOrderStrategy,
                playerNames = request.playerNames,
            ),
        )

    @GetMapping("/{id}")
    @Operation(summary = "템플릿 조회", operationId = "getTemplate")
    fun getById(
        @PathVariable id: Long,
    ): TemplateResponse {
        val template = templateLookUpService.get(TemplateIdentity.of(id))
        val players = templateLookUpService.getPlayers(template.templateId)
        return TemplateResponse.from(template, players)
    }

    @GetMapping
    @Operation(summary = "템플릿 목록 조회", operationId = "listTemplates")
    fun list(): List<TemplateResponse> = templateLookUpService.getAll().map { TemplateResponse.from(it) }
}
