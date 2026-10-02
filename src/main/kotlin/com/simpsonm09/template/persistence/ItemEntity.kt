package com.simpsonm09.template.persistence

import com.simpsonm09.template.domain.Item
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "items")
class ItemEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  var id: Long? = null,
  @Column(nullable = false)
  var name: String = "",
  @Column
  var description: String? = null,
) {
  fun toDomain(): Item = Item(id = id, name = name, description = description)

  companion object {
    fun fromDomain(item: Item): ItemEntity = ItemEntity(id = item.id, name = item.name, description = item.description)
  }
}
