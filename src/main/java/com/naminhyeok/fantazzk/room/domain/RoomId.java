package com.naminhyeok.fantazzk.room.domain;

import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public record RoomId(UUID roomId) implements Identifier {
}
