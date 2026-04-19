package com.naminhyeok.fantazzk.template.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.template.TemplateCatalog;
import com.naminhyeok.fantazzk.template.domain.TemplateErrorType;
import com.naminhyeok.fantazzk.template.domain.TemplateId;
import com.naminhyeok.fantazzk.template.query.FindTemplates;
import com.naminhyeok.fantazzk.template.query.TemplateDetail;
import com.naminhyeok.fantazzk.template.query.TemplateExternalViewMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProvideTemplateCatalog implements TemplateCatalog {
    private final FindTemplates findTemplates;

    @Override
    public TemplateBlueprint getTemplate(UUID templateId) {
        try {
            TemplateDetail detail = findTemplates.getDetail(new TemplateId(templateId));
            return TemplateExternalViewMapper.toBlueprint(detail);
        } catch (CoreException ex) {
            if (ex.getError() == TemplateErrorType.TEMPLATE_NOT_FOUND) {
                throw new TemplateCatalog.NotFound(templateId);
            }
            throw ex;
        }
    }
}
