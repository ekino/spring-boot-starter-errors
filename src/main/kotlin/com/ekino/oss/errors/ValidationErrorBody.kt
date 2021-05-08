package com.ekino.oss.errors

public data class ValidationErrorBody(
  val code: String,
  val field: String? = null,
  val message: String? = null
)
