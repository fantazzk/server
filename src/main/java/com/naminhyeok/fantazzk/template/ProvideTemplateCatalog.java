package com.naminhyeok.fantazzk.template;

import com.naminhyeok.fantazzk.CoreException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ProvideTemplateCatalog implements TemplateCatalog {
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
