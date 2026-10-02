package com.simpsonm09.template.domain

/**
 * An item as the service layer reasons about it, free of persistence and transport detail.
 * `id` is null until the item is persisted, at which point the store assigns it.
 */
data class Item(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
)
