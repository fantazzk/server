package com.naminhyeok.fantazzk.template

interface TemplateModel : TemplateIdentity, TemplateProps, AuditProps

val TemplateModel.picksPerTeam: Int get() = teamSize - 1
