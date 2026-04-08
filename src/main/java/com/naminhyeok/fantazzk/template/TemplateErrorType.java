package com.naminhyeok.fantazzk.template;

import com.naminhyeok.fantazzk.ErrorDescriptor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.slf4j.event.Level;

@Getter
enum TemplateErrorType implements ErrorDescriptor {
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "TEMPLATE_NOT_FOUND", "템플릿을 찾을 수 없습니다", Level.WARN),
    TEMPLATE_AUCTION_DRAFT_ORDER_STRATEGY_NOT_ALLOWED(
        HttpStatus.BAD_REQUEST,
        "TEMPLATE_AUCTION_DRAFT_ORDER_STRATEGY_NOT_ALLOWED",
        "경매 템플릿에는 드래프트 순서 전략을 지정할 수 없습니다",
        Level.INFO
    ),
    TEMPLATE_AUCTION_BUDGET_REQUIRED(
        HttpStatus.BAD_REQUEST,
        "TEMPLATE_AUCTION_BUDGET_REQUIRED",
        "경매 템플릿에는 예산이 필요합니다",
        Level.INFO
    ),
    TEMPLATE_DRAFT_BUDGET_NOT_ALLOWED(
        HttpStatus.BAD_REQUEST,
        "TEMPLATE_DRAFT_BUDGET_NOT_ALLOWED",
        "드래프트 템플릿에는 예산을 지정할 수 없습니다",
        Level.INFO
    ),
    TEMPLATE_DRAFT_ORDER_STRATEGY_REQUIRED(
        HttpStatus.BAD_REQUEST,
        "TEMPLATE_DRAFT_ORDER_STRATEGY_REQUIRED",
        "드래프트 템플릿에는 순서 전략이 필요합니다",
        Level.INFO
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final Level logLevel;

    TemplateErrorType(HttpStatus status, String code, String message, Level logLevel) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
    }
}
