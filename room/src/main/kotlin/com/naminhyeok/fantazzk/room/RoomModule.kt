package com.naminhyeok.fantazzk.room

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

@PackageInfo
@ApplicationModule(allowedDependencies = ["template :: api"])
class RoomModule
