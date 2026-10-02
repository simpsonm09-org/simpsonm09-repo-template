package com.simpsonm09.template.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
  @ExceptionHandler(ItemNotFoundException::class)
  fun handleItemNotFound(exception: ItemNotFoundException): ProblemDetail =
    ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.message ?: "Item was not found").apply {
      title = "Item not found"
    }
}
