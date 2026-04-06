package com.naminhyeok.fantazzk.room;

import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.ApplicationModule.Type;

@ApplicationModule(
    allowedDependencies = { "template" },
    type = Type.CLOSED
)
public final class RoomModuleMetadata {
}
