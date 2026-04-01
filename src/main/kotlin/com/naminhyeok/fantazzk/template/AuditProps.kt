package com.naminhyeok.fantazzk.template

import java.time.Instant

interface AuditProps {
    val createdAt: Instant
    val updatedAt: Instant
}
