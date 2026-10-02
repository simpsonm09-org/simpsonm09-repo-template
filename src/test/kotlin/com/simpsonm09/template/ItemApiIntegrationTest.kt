package com.simpsonm09.template

import com.simpsonm09.template.persistence.ItemJpaRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class ItemApiIntegrationTest {
  @Autowired
  lateinit var mockMvc: MockMvc

  @Autowired
  lateinit var repository: ItemJpaRepository

  @BeforeEach
  fun resetStore() {
    repository.deleteAll()
  }

  @Test
  fun `drives the full create, read, update, delete lifecycle`() {
    val body =
      mockMvc
        .perform(
          post("/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Widget","description":"A small widget"}"""),
        ).andExpect(status().isCreated)
        .andExpect(jsonPath("$.name").value("Widget"))
        .andReturn()
        .response.contentAsString

    val id =
      requireNotNull(Regex("\"id\"\\s*:\\s*(\\d+)").find(body)) {
        "response had no id: $body"
      }.groupValues[1]

    mockMvc
      .perform(get("/items"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].name").value("Widget"))

    mockMvc
      .perform(get("/items/$id"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.description").value("A small widget"))

    mockMvc
      .perform(
        put("/items/$id")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""{"name":"Renamed","description":"Still here"}"""),
      ).andExpect(status().isOk)
      .andExpect(jsonPath("$.name").value("Renamed"))

    mockMvc
      .perform(delete("/items/$id"))
      .andExpect(status().isNoContent)

    mockMvc
      .perform(get("/items/$id"))
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns 404 for an unknown id`() {
    mockMvc
      .perform(get("/items/999999"))
      .andExpect(status().isNotFound)
  }
}
