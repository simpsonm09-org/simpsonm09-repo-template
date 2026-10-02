package com.simpsonm09.template.service

import com.simpsonm09.template.domain.Item
import com.simpsonm09.template.exception.ItemNotFoundException
import com.simpsonm09.template.persistence.ItemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ItemService(
  private val repository: ItemRepository,
) {
  @Transactional(readOnly = true)
  fun listItems(): List<Item> = repository.findAll()

  @Transactional(readOnly = true)
  fun getItem(id: Long): Item = repository.findById(id) ?: throw ItemNotFoundException(id)

  @Transactional
  fun createItem(
    name: String,
    description: String?,
  ): Item = repository.save(Item(name = name, description = description))

  @Transactional
  fun updateItem(
    id: Long,
    name: String,
    description: String?,
  ): Item {
    if (!repository.existsById(id)) throw ItemNotFoundException(id)
    return repository.save(Item(id = id, name = name, description = description))
  }

  @Transactional
  fun deleteItem(id: Long) {
    if (!repository.existsById(id)) throw ItemNotFoundException(id)
    repository.deleteById(id)
  }
}
