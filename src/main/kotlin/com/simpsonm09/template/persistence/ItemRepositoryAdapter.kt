package com.simpsonm09.template.persistence

import com.simpsonm09.template.domain.Item
import org.springframework.stereotype.Repository

/** Adapts [ItemJpaRepository] to the domain-facing [ItemRepository] port. */
@Repository
class ItemRepositoryAdapter(
    private val jpa: ItemJpaRepository,
) : ItemRepository {
    override fun findAll(): List<Item> = jpa.findAll().map(ItemEntity::toDomain)

    override fun findById(id: Long): Item? = jpa.findById(id).map(ItemEntity::toDomain).orElse(null)

    override fun save(item: Item): Item = jpa.save(ItemEntity.fromDomain(item)).toDomain()

    override fun deleteById(id: Long) = jpa.deleteById(id)

    override fun existsById(id: Long): Boolean = jpa.existsById(id)
}
