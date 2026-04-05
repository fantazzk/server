package com.naminhyeok.fantazzk.template

import org.jmolecules.ddd.types.Identifier

data class TemplateId(
    val value: Long,
) : Identifier {
    init {
        require(value > 0) { "TemplateId는 1 이상이어야 합니다" }
    }
}
