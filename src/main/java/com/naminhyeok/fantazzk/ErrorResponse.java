package com.naminhyeok.fantazzk;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class ErrorResponse {
    private final int status;
    private final String errorCode;
    private final String reason;
}
