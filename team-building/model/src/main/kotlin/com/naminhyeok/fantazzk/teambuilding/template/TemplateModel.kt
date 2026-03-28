package com.naminhyeok.fantazzk.teambuilding.template

import com.naminhyeok.fantazzk.teambuilding.AuditProps

interface TemplateModel : TemplateIdentity, TemplateProps, AuditProps

val TemplateModel.picksPerTeam: Int get() = teamSize - 1
