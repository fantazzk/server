package com.naminhyeok.fantazzk.template.repository;

import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplateId;
import java.util.List;
import java.util.Optional;
import org.jmolecules.ddd.types.Repository;

public interface Templates extends Repository<Template, TemplateId> {
    Template save(Template template);

    Optional<Template> findById(TemplateId id);

    List<Template> findAll();
}
