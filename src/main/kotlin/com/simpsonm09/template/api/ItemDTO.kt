package com.simpsonm09.template.api

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "An item returned by the API")
data class ItemResponse(
    @field:Schema(description = "Server-assigned identifier", example = "1")
    val id: Long,
    @field:Schema(description = "Item name", example = "Widget")
    val name: String,
    @field:Schema(description = "Item description", example = "A small widget")
    val description: String?,
)

@Schema(description = "Payload used to create or replace an item")
data class ItemRequest(
    @field:NotBlank
    @field:Size(max = 200)
    @field:Schema(description = "Item name", example = "Widget")
    val name: String,
    @field:Size(max = 2000)
    @field:Schema(description = "Item description", example = "A small widget")
    val description: String? = null,
)
