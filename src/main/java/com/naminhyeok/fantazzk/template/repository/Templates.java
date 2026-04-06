package com.naminhyeok.fantazzk.template.repository;

import com.naminhyeok.fantazzk.template.TemplateId;
import com.naminhyeok.fantazzk.template.domain.Template;
import java.util.List;
import org.jmolecules.ddd.annotation.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Repository
public interface Templates extends org.jmolecules.ddd.types.Repository<Template, TemplateId> {
    Template save(Template template);

    Template findById(TemplateId templateId);

    List<Template> findAll();
}

interface TemplateJpaStore extends JpaRepository<Template, TemplateId> {
    @Override
    <S extends Template> S save(S entity);

    @Override
    List<Template> findAll();
}

@Component
class TemplateRepositoryAdapter implements Templates {
    private final TemplateJpaStore store;

    TemplateRepositoryAdapter(TemplateJpaStore store) {
        this.store = store;
    }

    @Override
    public Template save(Template template) {
        return store.save(template);
    }

    @Override
    public Template findById(TemplateId templateId) {
        return store.findById(templateId).orElse(null);
    }

    @Override
    public List<Template> findAll() {
        return store.findAll();
    }
}
