package com.naminhyeok.fantazzk.room.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RoomOpenApiDocsTest {
    @Test
    fun `room create example uses uuid template id`() {
        assertThat(RoomOpenApiDocs.CREATE_ROOM_REQUEST_EXAMPLE).contains("123e4567-e89b-12d3-a456-426614174000")
        assertThat(RoomOpenApiDocs.CREATE_ROOM_REQUEST_EXAMPLE).doesNotContain("\"templateId\":1")
    }
}
