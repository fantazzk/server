package com.naminhyeok.fantazzk.room;

import java.util.Optional;
import org.jmolecules.ddd.types.Repository;

interface Games extends Repository<Game, GameId> {
    Game save(Game game);

    Optional<Game> findById(GameId id);
}
