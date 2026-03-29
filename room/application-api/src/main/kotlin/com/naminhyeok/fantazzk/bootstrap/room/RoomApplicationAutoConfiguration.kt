package com.naminhyeok.fantazzk.bootstrap.room

import com.naminhyeok.fantazzk.bootstrap.room.adapter.TemplateFetcherAdapter
import com.naminhyeok.fantazzk.room.outport.TemplateFetcher
import com.naminhyeok.fantazzk.template.TemplateLookUpService
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock

@AutoConfiguration
class RoomApplicationAutoConfiguration {
    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun openApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Room API")
                    .version("v1")
                    .description("팀 빌딩 방 관리"),
            )

    @Bean
    fun templateFetcher(templateLookUpService: TemplateLookUpService): TemplateFetcher = TemplateFetcherAdapter(templateLookUpService)
}
