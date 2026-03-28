package com.naminhyeok.fantazzk.teambuilding.model.room

data class PlayerPool(val players: List<Player>) {

    fun currentTarget(): Player? = players.firstOrNull { it.status == PlayerStatus.AVAILABLE }

    fun assignPlayer(name: String): PlayerPool {
        val updated = players.map { p ->
            if (p.name == name && p.status == PlayerStatus.AVAILABLE) {
                p.copy(status = PlayerStatus.ASSIGNED)
            } else {
                p
            }
        }
        return PlayerPool(updated)
    }

    fun moveCurrentToBack(): PlayerPool {
        val target = currentTarget() ?: return this
        val remaining = players.filter { it != target }
        return PlayerPool(remaining + target)
    }

    fun markRemainingAsUnassigned(): PlayerPool {
        val updated = players.map { p ->
            if (p.status == PlayerStatus.AVAILABLE) p.copy(status = PlayerStatus.UNASSIGNED) else p
        }
        return PlayerPool(updated)
    }
}
