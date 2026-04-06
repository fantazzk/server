package com.naminhyeok.fantazzk.room.domain

val Room.configuration: TeamBuildingConfiguration
    get() = TeamBuildingConfiguration.from(this)

val Room.progress: RoomProgress
    get() = RoomProgress.from(this)

val Room.picksPerTeam: Int
    get() = teamSize - 1
