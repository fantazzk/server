package com.naminhyeok.fantazzk.template.query;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.domain.Template;
import com.naminhyeok.fantazzk.template.domain.TemplateErrorType;
import com.naminhyeok.fantazzk.template.domain.TemplateId;
import com.naminhyeok.fantazzk.template.repository.Templates;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindTemplates {
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
