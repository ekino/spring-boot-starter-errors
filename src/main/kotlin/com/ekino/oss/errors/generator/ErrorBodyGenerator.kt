package com.ekino.oss.errors.generator

import com.ekino.oss.errors.DefaultErrorCode
import com.ekino.oss.errors.ErrorBody
import com.ekino.oss.errors.ValidationErrorBody
import org.springframework.http.HttpStatus
import java.time.Instant

fun notFound(service: String, devMessage: String?, stacktrace: String): ErrorBody {
  return toError(
    service,
    HttpStatus.NOT_FOUND,
    DefaultErrorCode.NOT_FOUND.value(),
    HttpStatus.NOT_FOUND.reasonPhrase,
    devMessage,
    stacktrace,
    emptyList(),
    emptyList()
  )
}

fun unavailable(service: String, code: String, devMessage: String?, stacktrace: String): ErrorBody {
  return toError(
    service,
    HttpStatus.SERVICE_UNAVAILABLE,
    code,
    HttpStatus.SERVICE_UNAVAILABLE.reasonPhrase,
    devMessage,
    stacktrace,
    emptyList(),
    emptyList()
  )
}

fun unAuthorized(service: String, code: String, devMessage: String?, stacktrace: String): ErrorBody {
  return toError(service, HttpStatus.UNAUTHORIZED, code, HttpStatus.UNAUTHORIZED.reasonPhrase, devMessage, stacktrace, emptyList(), emptyList())
}

fun forbidden(service: String, code: String, devMessage: String?, stacktrace: String): ErrorBody {
  return toError(service, HttpStatus.FORBIDDEN, code, HttpStatus.FORBIDDEN.reasonPhrase, devMessage, stacktrace, emptyList(), emptyList())
}

@JvmOverloads
fun badRequest(
  service: String,
  code: String,
  devMessage: String?,
  stacktrace: String,
  errors: List<ValidationErrorBody> = emptyList(),
  globalErrors: List<ValidationErrorBody> = emptyList()
): ErrorBody {
  return toError(service, HttpStatus.BAD_REQUEST, code, HttpStatus.BAD_REQUEST.reasonPhrase, devMessage, stacktrace, errors, globalErrors)
}

fun methodNotAllowed(service: String, code: String, devMessage: String?, stacktrace: String): ErrorBody {
  return toError(
    service,
    HttpStatus.METHOD_NOT_ALLOWED,
    code,
    HttpStatus.METHOD_NOT_ALLOWED.reasonPhrase,
    devMessage,
    stacktrace,
    emptyList(),
    emptyList()
  )
}

fun conflict(service: String, devMessage: String?, stacktrace: String): ErrorBody {
  return toError(
    service,
    HttpStatus.CONFLICT,
    DefaultErrorCode.CONFLICT.value(),
    HttpStatus.CONFLICT.reasonPhrase,
    devMessage,
    stacktrace,
    emptyList(),
    emptyList()
  )
}

fun preconditionFailed(service: String, devMessage: String, stacktrace: String): ErrorBody {
  return toError(
    service,
    HttpStatus.PRECONDITION_FAILED,
    "error.preconditionFailed",
    HttpStatus.PRECONDITION_FAILED.reasonPhrase,
    devMessage,
    stacktrace,
    emptyList(),
    emptyList()
  )
}

fun conflict(service: String, devMessage: String, stacktrace: String, errors: List<ValidationErrorBody>): ErrorBody {
  return toError(
    service,
    HttpStatus.CONFLICT,
    DefaultErrorCode.CONFLICT.value(),
    HttpStatus.CONFLICT.reasonPhrase,
    devMessage,
    stacktrace,
    errors,
    emptyList()
  )
}

fun unprocessableEntity(service: String, code: String, devMessage: String, stacktrace: String, errors: List<ValidationErrorBody>): ErrorBody {
  return toError(
    service,
    HttpStatus.UNPROCESSABLE_ENTITY,
    code,
    HttpStatus.UNPROCESSABLE_ENTITY.reasonPhrase,
    devMessage,
    stacktrace,
    errors,
    emptyList()
  )
}

fun unsupportedMediaType(service: String, code: String, devMessage: String?, stacktrace: String): ErrorBody {
  return toError(
    service,
    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
    code,
    HttpStatus.UNSUPPORTED_MEDIA_TYPE.reasonPhrase,
    devMessage,
    stacktrace,
    emptyList(),
    emptyList()
  )
}

@JvmOverloads
fun defaultError(
  service: String,
  httpStatus: HttpStatus,
  code: String,
  devMessage: String,
  stacktrace: String,
  errors: List<ValidationErrorBody> = emptyList()
): ErrorBody {
  return toError(service, httpStatus, code, httpStatus.reasonPhrase, devMessage, stacktrace, errors, emptyList())
}

private fun toError(
  service: String,
  status: HttpStatus,
  code: String,
  message: String,
  description: String?,
  stacktrace: String,
  errors: List<ValidationErrorBody>,
  globalErrors: List<ValidationErrorBody>
): ErrorBody {
  return ErrorBody(
    status = status.value(),
    code = code,
    message = message,
    description = description,
    errors = errors,
    globalErrors = globalErrors,
    service = service,
    stacktrace = stacktrace,
    timestamp = Instant.now()
  )
}
