package com.naminhyeok.fantazzk;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 응답 결과 타입")
public enum ResultType {
    SUCCESS,
    ERROR
}
