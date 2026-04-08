package com.naminhyeok.fantazzk.template;

import com.naminhyeok.fantazzk.ApiResponse;
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
class TemplateApiController {
    private final CreateTemplate createTemplate;
    private final FindTemplates findTemplates;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<TemplateResponse> create(@Valid @RequestBody CreateTemplateRequest request) {
        return ApiResponse.success(TemplateResponse.from(createTemplate.create(request.toCommand())));
    }

    @GetMapping("/{id}")
    ApiResponse<TemplateResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(TemplateResponse.from(findTemplates.getDetail(new TemplateId(id))));
    }

    @GetMapping
    ApiResponse<List<TemplateResponse>> list() {
        return ApiResponse.success(findTemplates.list().stream().map(TemplateResponse::from).toList());
    }
}
