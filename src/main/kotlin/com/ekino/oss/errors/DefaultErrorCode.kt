package com.ekino.oss.errors

internal enum class DefaultErrorCode(
  private val value: String
) {
  NOT_FOUND("error.not_found"),
  CONFLICT("error.conflict");

  internal fun value(): String {
    return this.value
  }
}
