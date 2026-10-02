package com.simpsonm09.template

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.File

/**
 * Boots the context, fetches the springdoc document, and writes it to docs/openapi.json.
 * `gradle generateOpenApi` (or `just spec`) runs only this class so the checked-in spec is
 * regenerated from code without a running server.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `writes the generated openapi document`() {
        val json = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        check(json.contains("\"openapi\"")) { "generated document has no openapi version" }

        val target = File("docs/openapi.json")
        target.parentFile.mkdirs()
        target.writeText(json.trimEnd() + "\n")
    }
}
