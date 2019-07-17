package com.ekino.oss.errors.handler

import com.ekino.oss.errors.ErrorBody
import com.ekino.oss.errors.generator.badRequest
import com.ekino.oss.errors.generator.conflict
import com.ekino.oss.errors.generator.defaultError
import com.ekino.oss.errors.generator.methodNotAllowed
import com.ekino.oss.errors.generator.unavailable
import com.ekino.oss.errors.property.ErrorsProperties
import org.hibernate.JDBCException
import org.slf4j.LoggerFactory
import org.springframework.core.NestedRuntimeException
import org.springframework.core.Ordered
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.annotation.Order
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.net.ConnectException
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException

@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
abstract class CoreExceptionHandler(
  private val applicationName: String,
  private val properties: ErrorsProperties
) {
  private val log = LoggerFactory.getLogger(this.javaClass.name)

  @ExceptionHandler(ConnectException::class)
  fun handleUnavailableServiceException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.error("Unavailable service : ", e)
    return toErrorResponse(unavailable(
      buildServiceName(req, applicationName), "error.unavailable", e.message, stacktrace(e, properties.displayFullStacktrace)
    ))
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationException(req: HttpServletRequest, e: MethodArgumentNotValidException): ResponseEntity<ErrorBody> {
    return prepareValidationResponse(req, e, e.bindingResult)
  }

  @ExceptionHandler(BindException::class)
  fun handleBindException(req: HttpServletRequest, e: BindException): ResponseEntity<ErrorBody> {
    return prepareValidationResponse(req, e, e.bindingResult)
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

    val errors = e.constraintViolations?.map { toValidationErrorBody(it) } ?: emptyList()

    return toErrorResponse(badRequest(
      buildServiceName(req, applicationName), INVALID_ERROR_PREFIX, e.message, stacktrace(e, properties.displayFullStacktrace), errors
    ))
  }

  @ExceptionHandler(HttpMessageNotReadableException::class)
  fun handleMessageNotReadableException(req: HttpServletRequest, e: HttpMessageNotReadableException): ResponseEntity<ErrorBody> {
    log.debug("Message not readable : ", e)
    return toErrorResponse(badRequest(
      buildServiceName(req, applicationName), "error.not_readable_json", e.message, stacktrace(e, properties.displayFullStacktrace)
    ))
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleArgumentTypeMismatchException(req: HttpServletRequest, e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorBody> {
    log.debug("Argument type mismatch : ", e)
    return toErrorResponse(badRequest(
      buildServiceName(req, applicationName), "error.argument_type_mismatch", e.message, stacktrace(e, properties.displayFullStacktrace)
    ))
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
  fun handleMethodNotSupportedException(req: HttpServletRequest, e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorBody> {
    log.debug("Method not supported : ", e)
    return toErrorResponse(methodNotAllowed(
      buildServiceName(req, applicationName), "error.method_not_allowed", e.message, stacktrace(e, properties.displayFullStacktrace)
    ))
  }

  @ExceptionHandler(DataIntegrityViolationException::class)
  fun handleConflict(e: DataIntegrityViolationException, req: HttpServletRequest): ResponseEntity<ErrorBody> {
    log.debug("Database conflict : ", e)

    val cause = e.cause
    if (cause is JDBCException) {
      return toErrorResponse(conflict(
        buildServiceName(req, applicationName), cause.sqlException.message, stacktrace(e, properties.displayFullStacktrace)
      ))
    }
    return toErrorResponse(conflict(buildServiceName(req, applicationName), e.message, stacktrace(e, properties.displayFullStacktrace)))
  }

  @ExceptionHandler(Throwable::class)
  fun handleException(req: HttpServletRequest, e: Throwable): ResponseEntity<ErrorBody> {
    log.error("Unexpected error : ", e)

    val responseStatus = AnnotationUtils.findAnnotation(e.javaClass, ResponseStatus::class.java)

    val status = responseStatus?.value ?: HttpStatus.INTERNAL_SERVER_ERROR
    val message = responseStatus?.reason ?: getMessageByEnvironment(e)

    return toErrorResponse(defaultError(
      buildServiceName(req, applicationName), status, "error." + upperCamelToSnakeCase(e.javaClass.simpleName),
      message, stacktrace(e, properties.displayFullStacktrace)
    ))
  }

  private fun getMessageByEnvironment(e: Throwable) =
    if (properties.displayFullStacktrace) {
      e.message ?: DEFAULT_INTERNAL_ERROR_MESSAGE
    } else {
      DEFAULT_INTERNAL_ERROR_MESSAGE
    }

  private fun prepareValidationResponse(req: HttpServletRequest, e: Exception, bindingResult: BindingResult): ResponseEntity<ErrorBody> {
    log.debug("Validation errors : ", e)

    val errors = bindingResult.fieldErrors.map { toValidationErrorBody(it) }

    return toErrorResponse(badRequest(buildServiceName(req, applicationName),
      "error.invalid." + lowerCamelToSnakeCase(bindingResult.objectName),
      e.message,
      stacktrace(e, properties.displayFullStacktrace),
      errors))
  }
}
