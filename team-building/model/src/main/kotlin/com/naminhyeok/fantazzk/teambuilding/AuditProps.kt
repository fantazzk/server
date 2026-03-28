package com.naminhyeok.fantazzk.teambuilding

import java.time.Instant

interface AuditProps {
    val createdAt: Instant
    val updatedAt: Instant
}
