package com.simpsonm09.template.service

import com.simpsonm09.template.domain.Item
import com.simpsonm09.template.exception.ItemNotFoundException
import com.simpsonm09.template.persistence.ItemRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ItemServiceTest {
  private val repository = mock<ItemRepository>()
  private val service = ItemService(repository)

  @Test
  fun `lists every item`() {
    val items = listOf(Item(1, "Widget", "A small widget"))
    whenever(repository.findAll()).thenReturn(items)

    assertThat(service.listItems()).isEqualTo(items)
  }

  @Test
  fun `returns an item by id`() {
    whenever(repository.findById(1)).thenReturn(Item(1, "Widget", null))

    assertThat(service.getItem(1).name).isEqualTo("Widget")
  }

  @Test
  fun `throws when the item is missing`() {
    whenever(repository.findById(9)).thenReturn(null)

    assertThatThrownBy { service.getItem(9) }
      .isInstanceOf(ItemNotFoundException::class.java)
      .hasMessageContaining("9")
  }

  @Test
  fun `creates an item`() {
    whenever(repository.save(any())).thenReturn(Item(2, "Gadget", "A handy gadget"))

    val created = service.createItem("Gadget", "A handy gadget")

    assertThat(created.id).isEqualTo(2)
    verify(repository).save(Item(name = "Gadget", description = "A handy gadget"))
  }

  @Test
  fun `updates an existing item`() {
    whenever(repository.existsById(1)).thenReturn(true)
    whenever(repository.save(any())).thenReturn(Item(1, "Renamed", null))

    assertThat(service.updateItem(1, "Renamed", null).name).isEqualTo("Renamed")
    verify(repository).save(Item(id = 1, name = "Renamed", description = null))
  }

  @Test
  fun `refuses to update a missing item`() {
    whenever(repository.existsById(404)).thenReturn(false)

    assertThatThrownBy { service.updateItem(404, "Nope", null) }
      .isInstanceOf(ItemNotFoundException::class.java)
    verify(repository, never()).save(any())
  }

  @Test
  fun `deletes an existing item`() {
    whenever(repository.existsById(1)).thenReturn(true)

    service.deleteItem(1)

    verify(repository).deleteById(1)
  }

  @Test
  fun `refuses to delete a missing item`() {
    whenever(repository.existsById(404)).thenReturn(false)

    assertThatThrownBy { service.deleteItem(404) }
      .isInstanceOf(ItemNotFoundException::class.java)
    verify(repository, never()).deleteById(any())
  }
}
