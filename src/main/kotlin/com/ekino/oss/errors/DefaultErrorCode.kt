package com.ekino.oss.errors

enum class DefaultErrorCode(
  private val value: String
) {
  NOT_FOUND("error.not_found"),
  CONFLICT("error.conflict");

  fun value(): String {
    return this.value
  }
}
