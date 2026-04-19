package com.naminhyeok.fantazzk.template;

import com.naminhyeok.fantazzk.ApiResponse;
import com.naminhyeok.fantazzk.OpenApiDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Tag(name = OpenApiDocumentation.TEMPLATE_TAG)
class TemplateApiController {
    private final CreateTemplate createTemplate;
    private final FindTemplates findTemplates;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "템플릿 생성",
        description = "방 생성에 사용할 템플릿을 등록합니다. 운영 중 FE가 직접 호출할 가능성보다, 관리/운영 도구에서 사용하는 성격이 강합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201",
        description = "템플릿 생성 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = OpenApiDocumentation.TEMPLATE_DETAIL_SUCCESS_EXAMPLE)
        )
    )
    ApiResponse<TemplateResponse> create(@Valid @RequestBody CreateTemplateRequest request) {
        return ApiResponse.success(TemplateResponse.from(createTemplate.create(request.toCommand())));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "템플릿 상세 조회",
        description = "방 생성 전에 특정 템플릿의 상세 규칙과 선수 풀을 확인합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "템플릿 상세 조회 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = OpenApiDocumentation.TEMPLATE_DETAIL_SUCCESS_EXAMPLE)
        )
    )
    ApiResponse<TemplateResponse> getById(
        @Parameter(description = "조회할 템플릿 ID", example = "11111111-1111-1111-1111-111111111111")
        @PathVariable UUID id
    ) {
        return ApiResponse.success(TemplateResponse.from(findTemplates.getDetail(new TemplateId(id))));
    }

    @GetMapping
    @Operation(
        summary = "템플릿 목록 조회",
        description = "웹의 첫 진입 단계에서 사용 가능한 방 룰 목록을 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "템플릿 목록 조회 성공",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(value = OpenApiDocumentation.TEMPLATE_LIST_SUCCESS_EXAMPLE)
        )
    )
    ApiResponse<List<TemplateResponse>> list() {
        return ApiResponse.success(findTemplates.list().stream().map(TemplateResponse::from).toList());
    }
}
