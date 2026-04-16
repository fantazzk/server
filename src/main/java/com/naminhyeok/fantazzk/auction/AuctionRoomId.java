package com.naminhyeok.fantazzk.auction;

import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public record AuctionRoomId(UUID value) implements Identifier {
}
