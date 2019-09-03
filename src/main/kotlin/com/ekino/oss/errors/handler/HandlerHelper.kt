package com.ekino.oss.errors.handler

import com.ekino.oss.errors.ErrorBody
import com.ekino.oss.errors.ValidationErrorBody
import com.google.common.base.CaseFormat
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolation

const val DEFAULT_INTERNAL_ERROR_MESSAGE = "An internal error occurred on processing request."
const val INVALID_ERROR_PREFIX = "error.invalid"
const val MISSING_ERROR_PREFIX = "error.missing"

fun ErrorBody.toErrorResponse(httpHeaders: HttpHeaders? = null): ResponseEntity<ErrorBody> {
  return ResponseEntity(this, httpHeaders, HttpStatus.valueOf(this.status))
}

fun ObjectError.toValidationErrorBody(): ValidationErrorBody {
  val errorCodePrefix: String = if (this.code == null) {
    INVALID_ERROR_PREFIX
  } else {
    when (this.code) {
      "NotNull", "NotBlank", "NotEmpty" -> MISSING_ERROR_PREFIX
      else -> INVALID_ERROR_PREFIX
    }
  }

  return if (this is FieldError) {
    ValidationErrorBody(
      code = toErrorCode(errorCodePrefix, this.field),
      field = this.field,
      message = this.defaultMessage
    )
  } else {
    ValidationErrorBody(
      code = toErrorCode(errorCodePrefix, null),
      message = this.defaultMessage
    )
  }
}

fun ConstraintViolation<*>.toValidationErrorBody(): ValidationErrorBody {
  val fieldName = this.propertyPath?.toString()
  val errorCode = toErrorCode(INVALID_ERROR_PREFIX, fieldName)

  return ValidationErrorBody(
    code = errorCode,
    field = fieldName,
    message = this.message
  )
}

fun toErrorCode(errorPrefix: String, fieldName: String?): String {
  val field = if (fieldName.isNullOrBlank()) {
    "unknown"
  } else {
    ".${fieldName.toLowerCamelToSnakeCase()}"
  }

  return errorPrefix + field
}

fun String.toLowerCamelToSnakeCase(): String = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this)

fun String.toUpperCamelToSnakeCase(): String = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this)

fun Throwable.toStacktrace(displayFullStacktrace: Boolean): String {
  return if (displayFullStacktrace) ExceptionUtils.getStackTrace(this) else ""
}

fun HttpServletRequest.toServiceName(applicationName: String): String {
  return "$applicationName : ${this.method} ${this.requestURI}"
}
