package com.naminhyeok.fantazzk.template;

public class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException() {
        super("템플릿을 찾을 수 없습니다");
    }
}
