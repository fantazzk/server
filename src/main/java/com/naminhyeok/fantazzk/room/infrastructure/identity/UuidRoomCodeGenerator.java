package com.naminhyeok.fantazzk.room.infrastructure.identity;

import com.naminhyeok.fantazzk.room.application.port.RoomCodeGenerator;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class UuidRoomCodeGenerator implements RoomCodeGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
