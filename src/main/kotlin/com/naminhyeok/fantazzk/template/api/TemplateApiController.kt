package com.naminhyeok.fantazzk.template.api

import com.naminhyeok.fantazzk.template.TeamBuildingMode
import com.naminhyeok.fantazzk.template.TemplateId
import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand
import com.naminhyeok.fantazzk.template.application.TemplateCreateService
import com.naminhyeok.fantazzk.template.application.TemplateFinder
import com.naminhyeok.fantazzk.template.dto.ApiResponse
import com.naminhyeok.fantazzk.template.dto.CreateTemplateRequest
import com.naminhyeok.fantazzk.template.dto.TemplateResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@Tag(name = "Template", description = TemplateOpenApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/api/v1/templates")
class TemplateApiController(
    private val templateCreateService: TemplateCreateService,
    private val templateFinder: TemplateFinder,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "템플릿 생성", operationId = "createTemplate", description = TemplateOpenApiDocs.CREATE_DESCRIPTION)
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "201",
                description = "템플릿 생성 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "createdTemplate", value = TemplateOpenApiDocs.CREATED_TEMPLATE_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "400",
                description = "요청 값이 템플릿 생성 규칙을 만족하지 않습니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [
                            ExampleObject(
                                name = "teamCountMustBePositive",
                                value = TemplateOpenApiDocs.TEMPLATE_TEAM_COUNT_BAD_REQUEST_RESPONSE,
                            ),
                            ExampleObject(
                                name = "teamSizeMustBePositive",
                                value = TemplateOpenApiDocs.TEMPLATE_TEAM_SIZE_BAD_REQUEST_RESPONSE,
                            ),
                            ExampleObject(
                                name = "budgetMustBePositive",
                                value = TemplateOpenApiDocs.TEMPLATE_BUDGET_BAD_REQUEST_RESPONSE,
                            ),
                            ExampleObject(
                                name = "playerCountMustMatch",
                                value = TemplateOpenApiDocs.TEMPLATE_PLAYER_COUNT_BAD_REQUEST_RESPONSE,
                            ),
                        ],
                    ),
                ],
            ),
        ],
    )
    fun create(
        @SwaggerRequestBody(
            required = true,
            description = "생성할 템플릿의 이름, 모드, 팀 구성 정보, 선수 목록입니다.",
            content = [
                Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = [
                        ExampleObject(name = "createAuctionTemplate", value = TemplateOpenApiDocs.CREATE_AUCTION_TEMPLATE_REQUEST_EXAMPLE),
                        ExampleObject(name = "createDraftTemplate", value = TemplateOpenApiDocs.CREATE_DRAFT_TEMPLATE_REQUEST_EXAMPLE),
                    ],
                ),
            ],
        )
        @RequestBody request: CreateTemplateRequest,
    ): ApiResponse<TemplateResponse> =
        ApiResponse.success(
            TemplateResponse.from(
                templateCreateService.create(request.toCommand()),
            ),
        )

    @GetMapping("/{id}")
    @Operation(summary = "템플릿 조회", operationId = "getTemplate", description = TemplateOpenApiDocs.GET_DESCRIPTION)
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "템플릿 조회 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "templateDetail", value = TemplateOpenApiDocs.TEMPLATE_DETAIL_RESPONSE)],
                    ),
                ],
            ),
            SwaggerApiResponse(
                responseCode = "404",
                description = "존재하지 않는 템플릿입니다",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "templateNotFound", value = TemplateOpenApiDocs.TEMPLATE_NOT_FOUND_RESPONSE)],
                    ),
                ],
            ),
        ],
    )
    fun getById(
        @Parameter(description = TemplateOpenApiDocs.TEMPLATE_ID_PARAMETER, example = "1")
        @PathVariable id: Long,
    ): ApiResponse<TemplateResponse> {
        return ApiResponse.success(TemplateResponse.from(templateFinder.getDetail(TemplateId(id))))
    }

    @GetMapping
    @Operation(summary = "템플릿 목록 조회", operationId = "listTemplates", description = TemplateOpenApiDocs.LIST_DESCRIPTION)
    @ApiResponses(
        value = [
            SwaggerApiResponse(
                responseCode = "200",
                description = "템플릿 목록 조회 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        examples = [ExampleObject(name = "templateList", value = TemplateOpenApiDocs.TEMPLATE_LIST_RESPONSE)],
                    ),
                ],
            ),
        ],
    )
    fun list(): ApiResponse<List<TemplateResponse>> = ApiResponse.success(templateFinder.list().map(TemplateResponse::from))

    private fun CreateTemplateRequest.toCommand(): CreateTemplateCommand =
        when (mode) {
            TeamBuildingMode.AUCTION -> {
                require(draftOrderStrategy == null) { "경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다" }
                CreateTemplateCommand.Auction(
                    name = name,
                    teamCount = teamCount,
                    teamSize = teamSize,
                    budget = requireNotNull(budget) { "경매 템플릿에는 예산이 필요합니다" },
                    playerNames = playerNames,
                )
            }

            TeamBuildingMode.DRAFT -> {
                require(budget == null) { "드래프트 템플릿에는 예산을 지정할 수 없습니다" }
                CreateTemplateCommand.Draft(
                    name = name,
                    teamCount = teamCount,
                    teamSize = teamSize,
                    strategy = requireNotNull(draftOrderStrategy) { "드래프트 템플릿에는 순서 전략이 필요합니다" },
                    playerNames = playerNames,
                )
            }
        }
}
