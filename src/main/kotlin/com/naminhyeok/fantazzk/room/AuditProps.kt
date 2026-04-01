package com.naminhyeok.fantazzk.room

import java.time.Instant

interface AuditProps {
    val createdAt: Instant
    val updatedAt: Instant
}
