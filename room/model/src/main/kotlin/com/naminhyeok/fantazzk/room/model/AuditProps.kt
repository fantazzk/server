package com.naminhyeok.fantazzk.room.model

import java.time.Instant

interface AuditProps {
    val createdAt: Instant
    val updatedAt: Instant
}
