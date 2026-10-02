package com.simpsonm09.template.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
  @Bean
  fun templateOpenApi(): OpenAPI =
    OpenAPI().info(
      Info()
        .title("Simpsonm09 Template API")
        .version("0.1.0")
        .description("Item CRUD service for the simpsonm09 repository template"),
    )
}
