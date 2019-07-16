package com.ekino.oss.errors

data class ValidationErrorBody(
  val code: String,
  val field: String? = null,
  val message: String? = null
)
