package com.simpsonm09.template.api

import com.simpsonm09.template.domain.Item
import com.simpsonm09.template.exception.GlobalExceptionHandler
import com.simpsonm09.template.exception.ItemNotFoundException
import com.simpsonm09.template.service.ItemService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ItemControllerTest {
  private val service = mock<ItemService>()
  private val mockMvc: MockMvc =
    MockMvcBuilders
      .standaloneSetup(ItemController(service))
      .setControllerAdvice(GlobalExceptionHandler())
      .build()

  @Test
  fun `GET items returns the list`() {
    whenever(service.listItems()).thenReturn(listOf(Item(1, "Widget", null)))

    mockMvc
      .perform(get("/items"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].name").value("Widget"))
  }

  @Test
  fun `GET item by id returns it`() {
    whenever(service.getItem(1)).thenReturn(Item(1, "Widget", "A small widget"))

    mockMvc
      .perform(get("/items/1"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.description").value("A small widget"))
  }

  @Test
  fun `GET a missing item returns 404`() {
    whenever(service.getItem(7)).thenThrow(ItemNotFoundException(7))

    mockMvc
      .perform(get("/items/7"))
      .andExpect(status().isNotFound)
  }

  @Test
  fun `POST creates an item`() {
    whenever(service.createItem("Widget", "A small widget")).thenReturn(Item(1, "Widget", "A small widget"))

    mockMvc
      .perform(
        post("/items")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""{"name":"Widget","description":"A small widget"}"""),
      ).andExpect(status().isCreated)
      .andExpect(jsonPath("$.id").value(1))
  }

  @Test
  fun `POST with a blank name returns 400`() {
    mockMvc
      .perform(
        post("/items")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""{"name":""}"""),
      ).andExpect(status().isBadRequest)
  }

  @Test
  fun `PUT replaces an item`() {
    whenever(service.updateItem(1, "Renamed", null)).thenReturn(Item(1, "Renamed", null))

    mockMvc
      .perform(
        put("/items/1")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""{"name":"Renamed"}"""),
      ).andExpect(status().isOk)
      .andExpect(jsonPath("$.name").value("Renamed"))
  }

  @Test
  fun `DELETE removes an item`() {
    mockMvc
      .perform(delete("/items/1"))
      .andExpect(status().isNoContent)
  }
}
