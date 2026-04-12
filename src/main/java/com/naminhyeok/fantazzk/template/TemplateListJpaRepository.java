package com.naminhyeok.fantazzk.template;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

interface TemplateListJpaRepository extends Repository<Template, TemplateId> {
    @EntityGraph(attributePaths = "players")
    List<Template> findAll();
}
