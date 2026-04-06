package com.naminhyeok.fantazzk.template.repository;

import com.naminhyeok.fantazzk.template.TemplateId;
import com.naminhyeok.fantazzk.template.domain.Template;
import java.util.List;
import java.util.UUID;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.lang.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface Templates extends org.jmolecules.ddd.types.Repository<Template, TemplateId> {
    Template save(Template template);

    @Nullable
    Template findById(TemplateId templateId);

    List<Template> findAll();
}

interface TemplateJpaStore extends JpaRepository<Template, UUID>, Templates {
    @Override
    Template save(Template template);

    @Override
    List<Template> findAll();

    @Override
    @Nullable
    default Template findById(TemplateId templateId) {
        return findById(templateId.getValue()).orElse(null);
    }
}
