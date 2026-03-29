package com.naminhyeok.fantazzk.room.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ApiResponseTest {
    @Nested
    inner class `성공 응답` {
        @Test
        fun `데이터를 감싸서 SUCCESS 타입으로 반환한다`() {
            val cut = ApiResponse.success("hello")

            assertThat(cut.resultType).isEqualTo(ApiResponse.ResultType.SUCCESS)
            assertThat(cut.success).isEqualTo("hello")
            assertThat(cut.error).isNull()
        }
    }

    @Nested
    inner class `에러 응답` {
        @Test
        fun `에러 정보를 담아 ERROR 타입으로 반환한다`() {
            val cut = ApiResponse.error(404, "NOT_FOUND", "찾을 수 없습니다")

            assertThat(cut.resultType).isEqualTo(ApiResponse.ResultType.ERROR)
            assertThat(cut.success as Any?).isNull()
            assertThat(cut.error).isNotNull()
            assertThat(cut.error!!.status).isEqualTo(404)
            assertThat(cut.error!!.errorCode).isEqualTo("NOT_FOUND")
            assertThat(cut.error!!.reason).isEqualTo("찾을 수 없습니다")
            assertThat(cut.error!!.data).isNull()
        }

        @Test
        fun `에러에 추가 데이터를 포함할 수 있다`() {
            val data = mapOf("field" to "값이 필요합니다")
            val cut = ApiResponse.error(400, "VALIDATION_ERROR", "검증 실패", data)

            assertThat(cut.error!!.data).isEqualTo(data)
        }
    }
}
