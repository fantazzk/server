package com.naminhyeok.fantazzk.room.repository;

import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameId;
import java.util.Optional;
import org.jmolecules.ddd.types.Repository;

public interface Games extends Repository<Game, GameId> {
    public Game save(Game game);

    public Optional<Game> findById(GameId id);
}
