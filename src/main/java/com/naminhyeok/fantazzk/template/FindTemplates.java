package com.naminhyeok.fantazzk.template;

import com.naminhyeok.fantazzk.CoreException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@org.jmolecules.ddd.annotation.Service
@RequiredArgsConstructor
class FindTemplates {
    private final Templates templates;
    private final TemplateListReader templateListReader;

    @Transactional(readOnly = true)
    public TemplateDetail getDetail(TemplateId id) {
        Template template = templates.findById(id).orElseThrow(() -> CoreException.of(TemplateErrorType.TEMPLATE_NOT_FOUND));
        return new TemplateDetail(template, template.getPlayers());
    }

    @Transactional(readOnly = true)
    public List<TemplateDetail> list() {
        return templateListReader.list();
    }
}
