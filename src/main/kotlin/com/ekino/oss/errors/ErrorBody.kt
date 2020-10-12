package com.ekino.oss.errors

import java.time.Instant

public data class ErrorBody(
  val status: Int,
  val code: String,
  val message: String,
  val description: String?,
  val errors: List<ValidationErrorBody>,
  val globalErrors: List<ValidationErrorBody>,
  val service: String,
  val stacktrace: String,
  val timestamp: Instant
)
