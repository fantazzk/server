package com.naminhyeok.fantazzk.template.model

import java.time.Instant

interface AuditProps {
    val createdAt: Instant
    val updatedAt: Instant
}
