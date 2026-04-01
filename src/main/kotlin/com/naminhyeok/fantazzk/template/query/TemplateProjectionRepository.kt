package com.naminhyeok.fantazzk.template.query

import org.springframework.data.repository.CrudRepository

interface TemplateViewProjectionRepository {
    fun save(entity: TemplateViewEntity): TemplateViewEntity

    fun findById(templateId: Long): TemplateViewEntity?

    fun findAll(): List<TemplateViewEntity>
}

interface TemplatePlayerViewProjectionRepository {
    fun save(entity: TemplatePlayerViewEntity): TemplatePlayerViewEntity

    fun findByTemplateIdOrderByDisplayOrder(templateId: Long): List<TemplatePlayerViewEntity>
}

interface TemplateViewCrudRepository : CrudRepository<TemplateViewEntity, Long>, TemplateViewProjectionRepository

interface TemplatePlayerViewCrudRepository : CrudRepository<TemplatePlayerViewEntity, Long>, TemplatePlayerViewProjectionRepository
