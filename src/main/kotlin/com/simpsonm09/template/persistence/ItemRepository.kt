package com.simpsonm09.template.persistence

import com.simpsonm09.template.domain.Item

/**
 * The persistence port the service depends on. It speaks domain types, so the service
 * never sees a JPA entity and the store can be swapped without touching business logic.
 */
interface ItemRepository {
  fun findAll(): List<Item>

  fun findById(id: Long): Item?

  fun save(item: Item): Item

  fun deleteById(id: Long)

  fun existsById(id: Long): Boolean
}
