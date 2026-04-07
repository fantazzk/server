package com.naminhyeok.fantazzk.room;

import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public record RoomId(UUID roomId) implements Identifier {
}
