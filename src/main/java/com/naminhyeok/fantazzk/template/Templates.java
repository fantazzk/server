package com.naminhyeok.fantazzk.template;

import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.types.Repository;

public interface Templates extends Repository<Template, Template.TemplateId> {
    Template save(Template template);

    Optional<Template> findById(Template.TemplateId id);

    List<Template> findAll();
}
