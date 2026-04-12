package com.naminhyeok.fantazzk.template;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.jmolecules.ddd.types.Repository;

interface Templates extends Repository<Template, TemplateId> {
    Template save(Template template);

    Optional<Template> findById(TemplateId id);

    @EntityGraph(attributePaths = "players")
    List<Template> findAll();
}
