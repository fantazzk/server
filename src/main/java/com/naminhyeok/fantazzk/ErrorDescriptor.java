package com.naminhyeok.fantazzk;

import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

public interface ErrorDescriptor {
    HttpStatus getStatus();

    String getCode();

    String getMessage();

    LogLevel getLogLevel();
}
