package com.naminhyeok.fantazzk.template.web;

import com.naminhyeok.fantazzk.template.TemplateId;
import com.naminhyeok.fantazzk.template.application.CreateTemplate;
import com.naminhyeok.fantazzk.template.application.CreateTemplateCommand;
import com.naminhyeok.fantazzk.template.application.FindTemplates;
import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.template.domain.TeamBuildingMode;
import com.naminhyeok.fantazzk.template.domain.Template;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Template", description = TemplateOpenApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/api/v1/templates")
public class TemplateApiController {
    private final CreateTemplate templateCreateService;
    private final FindTemplates templateFinder;

    public TemplateApiController(CreateTemplate templateCreateService, FindTemplates templateFinder) {
        this.templateCreateService = templateCreateService;
        this.templateFinder = templateFinder;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "템플릿 생성",
        operationId = "createTemplate",
        description = TemplateOpenApiDocs.CREATE_DESCRIPTION
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(
        value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "템플릿 생성 성공",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "createdTemplate", value = TemplateOpenApiDocs.CREATED_TEMPLATE_RESPONSE)
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "요청 값이 템플릿 생성 규칙을 만족하지 않습니다",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = {
                        @ExampleObject(name = "teamCountMustBePositive", value = TemplateOpenApiDocs.TEMPLATE_TEAM_COUNT_BAD_REQUEST_RESPONSE),
                        @ExampleObject(name = "teamSizeMustBePositive", value = TemplateOpenApiDocs.TEMPLATE_TEAM_SIZE_BAD_REQUEST_RESPONSE),
                        @ExampleObject(name = "budgetMustBePositive", value = TemplateOpenApiDocs.TEMPLATE_BUDGET_BAD_REQUEST_RESPONSE),
                        @ExampleObject(name = "playerCountMustMatch", value = TemplateOpenApiDocs.TEMPLATE_PLAYER_COUNT_BAD_REQUEST_RESPONSE)
                    }
                )
            )
        }
    )
    public ApiResponse<TemplateResponse> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "생성할 템플릿의 이름, 모드, 팀 구성 정보, 선수 목록입니다.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = {
                    @ExampleObject(name = "createAuctionTemplate", value = TemplateOpenApiDocs.CREATE_AUCTION_TEMPLATE_REQUEST_EXAMPLE),
                    @ExampleObject(name = "createDraftTemplate", value = TemplateOpenApiDocs.CREATE_DRAFT_TEMPLATE_REQUEST_EXAMPLE)
                }
            )
        )
        @org.springframework.web.bind.annotation.RequestBody CreateTemplateRequest request
    ) {
        Template template = templateCreateService.create(toCommand(request));
        return ApiResponse.success(TemplateResponse.from(template, null));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "템플릿 조회",
        operationId = "getTemplate",
        description = TemplateOpenApiDocs.GET_DESCRIPTION
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(
        value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "템플릿 조회 성공",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "templateDetail", value = TemplateOpenApiDocs.TEMPLATE_DETAIL_RESPONSE)
                )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "존재하지 않는 템플릿입니다",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "templateNotFound", value = TemplateOpenApiDocs.TEMPLATE_NOT_FOUND_RESPONSE)
                )
            )
        }
    )
    public ApiResponse<TemplateResponse> getById(
        @Parameter(description = TemplateOpenApiDocs.TEMPLATE_ID_PARAMETER, example = "00000000-0000-0000-0000-000000000001")
        @PathVariable String id
    ) {
        return ApiResponse.success(TemplateResponse.from(templateFinder.getDetail(TemplateId.of(id))));
    }

    @GetMapping
    @Operation(
        summary = "템플릿 목록 조회",
        operationId = "listTemplates",
        description = TemplateOpenApiDocs.LIST_DESCRIPTION
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(
        value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "템플릿 목록 조회 성공",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "templateList", value = TemplateOpenApiDocs.TEMPLATE_LIST_RESPONSE)
                )
            )
        }
    )
    public ApiResponse<List<TemplateResponse>> list() {
        return ApiResponse.success(
            templateFinder.list().stream().map(template -> TemplateResponse.from(template, null)).collect(Collectors.toList())
        );
    }

    private CreateTemplateCommand toCommand(CreateTemplateRequest request) {
        if (request.getMode() == TeamBuildingMode.AUCTION) {
            if (request.getDraftOrderStrategy() != null) {
                throw new IllegalArgumentException("경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다");
            }
            Integer budget = request.getBudget();
            if (budget == null) {
                throw new IllegalArgumentException("경매 템플릿에는 예산이 필요합니다");
            }
            return new CreateTemplateCommand.Auction(
            request.getName(),
            request.getTeamCount(),
            request.getTeamSize(),
            budget,
            request.getPlayerNames()
        );
        }

        if (request.getBudget() != null) {
            throw new IllegalArgumentException("드래프트 템플릿에는 예산을 지정할 수 없습니다");
        }
        final DraftOrderStrategy draftOrderStrategy = request.getDraftOrderStrategy();
        if (draftOrderStrategy == null) {
            throw new IllegalArgumentException("드래프트 템플릿에는 순서 전략이 필요합니다");
        }
        return new CreateTemplateCommand.Draft(
            request.getName(),
            request.getTeamCount(),
            request.getTeamSize(),
            draftOrderStrategy,
            request.getPlayerNames()
        );
    }
}
