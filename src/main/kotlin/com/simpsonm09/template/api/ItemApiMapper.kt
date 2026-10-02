package com.simpsonm09.template.api

import com.simpsonm09.template.domain.Item

/** Translates between the transport DTOs and the domain type. */
object ItemApiMapper {
  fun toResponse(item: Item): ItemResponse =
    ItemResponse(
      id = requireNotNull(item.id) { "a persisted item must have an id" },
      name = item.name,
      description = item.description,
    )

  fun toDomain(request: ItemRequest): Item = Item(name = request.name, description = request.description)
}
