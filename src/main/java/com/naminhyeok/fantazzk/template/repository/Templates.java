package com.naminhyeok.fantazzk.template.repository;

import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplateId;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.types.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface Templates extends Repository<Template, TemplateId> {
    Template save(Template template);

    @EntityGraph(attributePaths = "players")
    Optional<Template> findById(TemplateId id);

    @EntityGraph(attributePaths = "players")
    List<Template> findAll();
}
