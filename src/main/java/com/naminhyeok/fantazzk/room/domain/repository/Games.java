package com.naminhyeok.fantazzk.room.domain.repository;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.GameId;

import java.util.Optional;
import org.jmolecules.ddd.types.Repository;

public interface Games extends Repository<Game, GameId> {
    Game save(Game game);

    Optional<Game> findById(GameId id);
}
