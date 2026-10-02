package com.simpsonm09.template.api

import com.simpsonm09.template.service.ItemService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/items")
@Tag(name = "Items", description = "CRUD operations for items")
class ItemController(
    private val service: ItemService,
) {
    @GetMapping
    @Operation(summary = "List every item")
    fun listItems(): List<ItemResponse> = service.listItems().map(ItemApiMapper::toResponse)

    @GetMapping("/{id}")
    @Operation(summary = "Get one item by id")
    fun getItem(@PathVariable id: Long): ItemResponse = ItemApiMapper.toResponse(service.getItem(id))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an item")
    fun createItem(@Valid @RequestBody request: ItemRequest): ItemResponse =
        ItemApiMapper.toResponse(service.createItem(request.name, request.description))

    @PutMapping("/{id}")
    @Operation(summary = "Replace an item")
    fun updateItem(@PathVariable id: Long, @Valid @RequestBody request: ItemRequest): ItemResponse =
        ItemApiMapper.toResponse(service.updateItem(id, request.name, request.description))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an item")
    fun deleteItem(@PathVariable id: Long) = service.deleteItem(id)
}
