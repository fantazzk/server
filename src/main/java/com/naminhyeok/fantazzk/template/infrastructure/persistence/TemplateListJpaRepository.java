package com.naminhyeok.fantazzk.template.infrastructure.persistence;

import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplateId;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

public interface TemplateListJpaRepository extends Repository<Template, TemplateId> {
    @EntityGraph(attributePaths = "players")
    List<Template> findAll();
}
