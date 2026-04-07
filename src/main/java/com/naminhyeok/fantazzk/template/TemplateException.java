package com.naminhyeok.fantazzk.template;

import com.naminhyeok.fantazzk.CoreException;
import java.util.Map;

public class TemplateException extends CoreException {
    private TemplateException(TemplateErrorType error, Object data) {
        super(error, data);
    }

    public static TemplateException notFound(TemplateId templateId) {
        return new TemplateException(TemplateErrorType.TEMPLATE_NOT_FOUND, Map.of("templateId", templateId.templateId().toString()));
    }

    public static TemplateException invalidRequest(String detail) {
        return new TemplateException(TemplateErrorType.TEMPLATE_INVALID_REQUEST, Map.of("detail", detail));
    }
}
