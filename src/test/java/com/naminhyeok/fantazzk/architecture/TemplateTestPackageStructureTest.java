package com.naminhyeok.fantazzk.architecture;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TemplateTestPackageStructureTest {
    @Test
    void template_test는_역할별_패키지에_위치한다() {
        assertClassMissing("com.naminhyeok.fantazzk.template.TemplateAggregateTest");
        assertClassMissing("com.naminhyeok.fantazzk.template.CreateTemplateTest");
        assertClassMissing("com.naminhyeok.fantazzk.template.FindTemplatesTest");
        assertClassMissing("com.naminhyeok.fantazzk.template.TemplateApiControllerWebMvcTest");

        assertClassPresent("com.naminhyeok.fantazzk.template.domain.TemplateAggregateTest");
        assertClassPresent("com.naminhyeok.fantazzk.template.application.CreateTemplateTest");
        assertClassPresent("com.naminhyeok.fantazzk.template.query.FindTemplatesTest");
        assertClassPresent("com.naminhyeok.fantazzk.template.web.TemplateApiControllerWebMvcTest");
    }

    private void assertClassMissing(String className) {
        assertThatThrownBy(() -> Class.forName(className))
            .isInstanceOf(ClassNotFoundException.class);
    }

    private void assertClassPresent(String className) {
        assertThatCode(() -> Class.forName(className)).doesNotThrowAnyException();
    }
}
