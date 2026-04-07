package com.naminhyeok.fantazzk;

import org.springframework.http.HttpStatus;
import org.slf4j.event.Level;

public interface ErrorDescriptor {
    HttpStatus getStatus();

    String getCode();

    String getMessage();

    Level getLogLevel();
}
