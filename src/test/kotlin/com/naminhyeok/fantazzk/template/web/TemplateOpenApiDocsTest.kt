package com.naminhyeok.fantazzk.template.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TemplateOpenApiDocsTest {
    @Test
    fun `template openapi examples use uuid template ids`() {
        val uuidRegex = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

        assertThat(TemplateOpenApiDocs.TEMPLATE_ID_PARAMETER).contains("UUID")
        assertThat(uuidRegex.containsMatchIn(TemplateOpenApiDocs.CREATED_TEMPLATE_RESPONSE)).isTrue()
        assertThat(uuidRegex.containsMatchIn(TemplateOpenApiDocs.TEMPLATE_DETAIL_RESPONSE)).isTrue()
        assertThat(uuidRegex.containsMatchIn(TemplateOpenApiDocs.TEMPLATE_LIST_RESPONSE)).isTrue()
        assertThat(TemplateOpenApiDocs.CREATED_TEMPLATE_RESPONSE).doesNotContain("\"id\":1")
    }
}
