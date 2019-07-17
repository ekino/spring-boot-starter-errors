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

fun toErrorResponse(errorBody: ErrorBody, httpHeaders: HttpHeaders? = null): ResponseEntity<ErrorBody> {
  return ResponseEntity(errorBody, httpHeaders, HttpStatus.valueOf(errorBody.status))
}

fun toValidationErrorBody(objectError: ObjectError): ValidationErrorBody {
  val errorCodePrefix: String = if (objectError.code == null) {
    INVALID_ERROR_PREFIX
  } else {
    when (objectError.code) {
      "NotNull", "NotBlank", "NotEmpty" -> MISSING_ERROR_PREFIX
      else -> INVALID_ERROR_PREFIX
    }
  }

  return if (objectError is FieldError) {
    ValidationErrorBody(
      code = toErrorCode(errorCodePrefix, objectError.field),
      field = objectError.field,
      message = objectError.defaultMessage
    )
  } else {
    ValidationErrorBody(
      code = toErrorCode(errorCodePrefix, null),
      message = objectError.defaultMessage
    )
  }
}

fun toValidationErrorBody(constraintViolation: ConstraintViolation<*>): ValidationErrorBody {
  val fieldName = constraintViolation.propertyPath?.toString()
  val errorCode = toErrorCode(INVALID_ERROR_PREFIX, fieldName)

  return ValidationErrorBody(
    code = errorCode,
    field = fieldName,
    message = constraintViolation.message
  )
}

fun toErrorCode(errorPrefix: String, fieldName: String?): String {
  val field = if (fieldName.isNullOrBlank()) {
    "unknown"
  } else {
    ".${lowerCamelToSnakeCase(fieldName)}"
  }

  return errorPrefix + field
}

fun lowerCamelToSnakeCase(label: String): String {
  return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, label)
}

fun upperCamelToSnakeCase(label: String): String {
  return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, label)
}

fun stacktrace(e: Throwable, displayFullStacktrace: Boolean): String {
  return if (displayFullStacktrace) ExceptionUtils.getStackTrace(e) else ""
}

fun buildServiceName(req: HttpServletRequest, applicationName: String): String {
  return "$applicationName : ${req.method} ${req.requestURI}"
}
