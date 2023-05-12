package com.ekino.oss.errors.handler

import com.ekino.oss.errors.ErrorBody
import com.ekino.oss.errors.generator.badRequest
import com.ekino.oss.errors.generator.defaultError
import com.ekino.oss.errors.generator.methodNotAllowed
import com.ekino.oss.errors.generator.notFound
import com.ekino.oss.errors.generator.unavailable
import com.ekino.oss.errors.generator.unsupportedMediaType
import com.ekino.oss.errors.property.ErrorsProperties
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.springframework.core.NestedRuntimeException
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.validation.BindException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import java.net.ConnectException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException

/**
 * Core handler for Spring common exceptions.
 * Ordered LOWEST_PRECEDENCE because the handler on [Throwable] must be the last one.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
abstract class CoreExceptionHandler(
  private val applicationName: String,
  private val properties: ErrorsProperties
) {
  private val log by logger()

  @ExceptionHandler(ConnectException::class)
  fun handleUnavailableServiceException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.error("Unavailable service : ", e)
    return unavailable(
      req.toServiceName(applicationName),
      "error.unavailable",
      e.message,
      e.toStacktrace(properties.displayFullStacktrace)
    ).toErrorResponse()
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationException(req: HttpServletRequest, e: MethodArgumentNotValidException): ResponseEntity<ErrorBody> {
    return prepareValidationResponse(req, e, "The request is invalid")
  }

  @ExceptionHandler(BindException::class)
  fun handleBindException(req: HttpServletRequest, e: BindException): ResponseEntity<ErrorBody> {
    return prepareValidationResponse(req, e, e.message)
  }

  @ExceptionHandler(NestedRuntimeException::class)
  fun handleNestedRuntimeException(req: HttpServletRequest, e: NestedRuntimeException): ResponseEntity<ErrorBody> {
    log.error("Nested runtime exception : ", e)

    val cause = e.mostSpecificCause
    return if (cause is ConstraintViolationException) {
      handleConstraintViolationException(req, cause)
    } else {
      handleException(req, e)
    }
  }

  @ExceptionHandler(ConstraintViolationException::class)
  fun handleConstraintViolationException(req: HttpServletRequest, e: ConstraintViolationException): ResponseEntity<ErrorBody> {
    log.debug("Constraint violation errors : ", e)

    val errors = e.constraintViolations?.map { it.toValidationErrorBody() } ?: emptyList()

    return badRequest(
      req.toServiceName(applicationName),
      INVALID_ERROR_PREFIX,
      e.message,
      e.toStacktrace(properties.displayFullStacktrace),
      errors
    ).toErrorResponse()
  }

  @ExceptionHandler(HttpMessageConversionException::class)
  fun handleMessageNotReadableException(req: HttpServletRequest, e: HttpMessageConversionException): ResponseEntity<ErrorBody> {
    log.debug("Message not readable : ", e)
    val cause = e.cause
    return if (cause is InvalidFormatException && cause.targetType.isEnum) {
      val validationErrorBody = listOf(cause.toEnumValidationErrorBody())
      val message = "The value '${cause.value}' is not an accepted value"
      badRequest(
        req.toServiceName(applicationName),
        "error.invalid.enum",
        message,
        e.toStacktrace(properties.displayFullStacktrace),
        validationErrorBody
      ).toErrorResponse()
    } else {
      badRequest(
        req.toServiceName(applicationName),
        "error.not_readable_json",
        e.message,
        e.toStacktrace(properties.displayFullStacktrace)
      ).toErrorResponse()
    }
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleArgumentTypeMismatchException(req: HttpServletRequest, e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorBody> {
    log.debug("Argument type mismatch : ", e)
    val requiredType = e.requiredType
    return if (requiredType != null && requiredType.isEnum) {
      val validationErrorBody = listOf(e.toEnumValidationErrorBody())
      val message = "The value '${e.value}' is not an accepted value"
      badRequest(
        req.toServiceName(applicationName),
        "error.invalid.enum",
        message,
        e.toStacktrace(properties.displayFullStacktrace),
        validationErrorBody
      ).toErrorResponse()
    } else {
      badRequest(
        req.toServiceName(applicationName),
        "error.argument_type_mismatch",
        e.message,
        e.toStacktrace(properties.displayFullStacktrace)
      ).toErrorResponse()
    }
  }

  @ExceptionHandler(MissingServletRequestParameterException::class)
  fun handleMissingServletRequestParameterException(req: HttpServletRequest, e: MissingServletRequestParameterException): ResponseEntity<ErrorBody> {
    log.debug("Missing parameter : ", e)
    return badRequest(
      req.toServiceName(applicationName),
      "error.missing_parameter",
      e.message,
      e.toStacktrace(properties.displayFullStacktrace)
    ).toErrorResponse()
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
  fun handleMethodNotSupportedException(req: HttpServletRequest, e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorBody> {
    log.debug("Method not supported : ", e)
    return methodNotAllowed(
      req.toServiceName(applicationName),
      "error.method_not_allowed",
      e.message,
      e.toStacktrace(properties.displayFullStacktrace)
    ).toErrorResponse()
  }

  @ExceptionHandler(NoHandlerFoundException::class)
  fun handleNoHandlerFoundException(req: HttpServletRequest, e: NoHandlerFoundException): ResponseEntity<ErrorBody> {
    log.trace("Resource not found : ", e)
    return notFound(req.toServiceName(applicationName), e.message, e.toStacktrace(properties.displayFullStacktrace)).toErrorResponse()
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
  fun handleHttpMediaTypeNotSupportedException(req: HttpServletRequest, e: HttpMediaTypeNotSupportedException): ResponseEntity<ErrorBody> {
    log.debug("Media type not supported : ", e)
    return unsupportedMediaType(
      req.toServiceName(applicationName),
      "error.media_type_not_supported",
      e.message,
      e.toStacktrace(properties.displayFullStacktrace)
    ).toErrorResponse()
  }

  @ExceptionHandler(Throwable::class)
  fun handleException(req: HttpServletRequest, e: Throwable): ResponseEntity<ErrorBody> {
    val responseStatus = AnnotationUtils.findAnnotation(e.javaClass, ResponseStatus::class.java)

    if (responseStatus == null) {
      log.error("Unexpected error : ", e)
    }

    val status = responseStatus?.value ?: HttpStatus.INTERNAL_SERVER_ERROR
    val message = responseStatus?.reason ?: e.toMessage()

    return defaultError(
      req.toServiceName(applicationName),
      status,
      "error." + e.javaClass.simpleName.camelToSnakeCase(),
      message,
      e.toStacktrace(properties.displayFullStacktrace)
    ).toErrorResponse()
  }

  private fun Throwable.toMessage() =
    if (properties.displayFullStacktrace) {
      this.message ?: DEFAULT_INTERNAL_ERROR_MESSAGE
    } else {
      DEFAULT_INTERNAL_ERROR_MESSAGE
    }

  private fun prepareValidationResponse(req: HttpServletRequest, e: BindException, message: String): ResponseEntity<ErrorBody> {
    log.debug("Validation errors : ", e)

    val bindingResult = e.bindingResult
    val errors = bindingResult.fieldErrors.map { it.toValidationErrorBody() }

    return badRequest(
      req.toServiceName(applicationName),
      "error.invalid." + bindingResult.objectName.camelToSnakeCase(),
      message,
      e.toStacktrace(properties.displayFullStacktrace),
      errors
    ).toErrorResponse()
  }
}
