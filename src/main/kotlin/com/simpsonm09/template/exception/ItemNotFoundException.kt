package com.simpsonm09.template.exception

/** Raised when an item id has no matching row. Mapped to HTTP 404 by the advice. */
class ItemNotFoundException(val itemId: Long) : RuntimeException("Item $itemId was not found")
