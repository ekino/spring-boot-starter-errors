package com.ekino.oss.errors

import com.ekino.oss.errors.generator.badRequest
import com.ekino.oss.errors.generator.conflict
import com.ekino.oss.errors.generator.defaultError
import com.ekino.oss.errors.generator.forbidden
import com.ekino.oss.errors.generator.methodNotAllowed
import com.ekino.oss.errors.generator.notFound
import com.ekino.oss.errors.generator.unAuthorized
import com.ekino.oss.errors.generator.unavailable
import com.ekino.oss.errors.property.ErrorsProperties
import com.google.common.base.CaseFormat
import org.apache.commons.lang3.exception.ExceptionUtils
import org.hibernate.JDBCException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.EnvironmentAware
import org.springframework.core.NestedRuntimeException
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.env.Environment
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.data.rest.webmvc.ResourceNotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.BindException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import java.net.ConnectException
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException

@RestControllerAdvice
class RestExceptionHandler(
  @param:Value("\${spring.application.name}") private val applicationName: String,
  val properties: ErrorsProperties
) : EnvironmentAware {
  private val log = LoggerFactory.getLogger(this.javaClass.name)

  private lateinit var environment: Environment

  @ExceptionHandler(ConnectException::class)
  fun handleUnavailableServiceException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.error("Unavailable service : ", e)
    return toErrorResponse(unavailable(buildServiceName(req), "error.unavailable", e.message, stacktrace(e)))
  }

  @ExceptionHandler(AuthenticationCredentialsNotFoundException::class, InsufficientAuthenticationException::class, UsernameNotFoundException::class)
  fun handleAuthenticationException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.debug("Authentication failed : ", e)
    return toErrorResponse(unAuthorized(buildServiceName(req), "error.unauthorized", e.message, stacktrace(e)))
  }

  @ExceptionHandler(AccessDeniedException::class, BadCredentialsException::class)
  fun handleAccessDeniedException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.debug("Access denied", e)
    return toErrorResponse(forbidden(buildServiceName(req), "error.access_denied", e.message, stacktrace(e)))
  }

  @ExceptionHandler(DisabledException::class)
  fun handleDisabledException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.debug("Disable account : ", e)
    return toErrorResponse(forbidden(buildServiceName(req), "error.disabled_account", e.message, stacktrace(e)))
  }

  @ExceptionHandler(ResourceNotFoundException::class)
  fun handleResourceNotFoundException(req: HttpServletRequest, e: ResourceNotFoundException): ResponseEntity<ErrorBody> {
    log.trace("Resource not found : ", e)
    return toErrorResponse(notFound(buildServiceName(req), e.message, stacktrace(e)))
  }

  @ExceptionHandler(NoHandlerFoundException::class)
  fun handleNoHandlerFoundException(req: HttpServletRequest, e: NoHandlerFoundException): ResponseEntity<ErrorBody> {
    log.trace("Resource not found : ", e)
    return toErrorResponse(notFound(buildServiceName(req), e.message, stacktrace(e)))
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

    return toErrorResponse(badRequest(buildServiceName(req), INVALID_ERROR_PREFIX, e.message, stacktrace(e), errors))
  }

  @ExceptionHandler(HttpMessageNotReadableException::class)
  fun handleMessageNotReadableException(req: HttpServletRequest, e: HttpMessageNotReadableException): ResponseEntity<ErrorBody> {
    log.debug("Message not readable : ", e)
    return toErrorResponse(badRequest(buildServiceName(req), "error.not_readable_json", e.message, stacktrace(e)))
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleArgumentTypeMismatchException(req: HttpServletRequest, e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorBody> {
    log.debug("Argument type mismatch : ", e)
    return toErrorResponse(badRequest(buildServiceName(req), "error.argument_type_mismatch", e.message, stacktrace(e)))
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
  fun handleMethodNotSupportedException(req: HttpServletRequest, e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorBody> {
    log.debug("Method not supported : ", e)
    return toErrorResponse(methodNotAllowed(buildServiceName(req), "error.method_not_allowed", e.message, stacktrace(e)))
  }

  @ExceptionHandler(RepositoryConstraintViolationException::class)
  fun handleRepositoryConstraintViolationException(
    e: RepositoryConstraintViolationException,
    req: HttpServletRequest
  ): ResponseEntity<ErrorBody> {

    log.debug("Constraint violation errors : ", e)

    val errors = e.errors.fieldErrors.map { toValidationErrorBody(it) }
    val globalErrors = e.errors.globalErrors.map { toValidationErrorBody(it) }

    return toErrorResponse(badRequest(buildServiceName(req), INVALID_ERROR_PREFIX, e.message, stacktrace(e), errors, globalErrors))
  }

  @ExceptionHandler(DataIntegrityViolationException::class)
  fun handleConflict(e: DataIntegrityViolationException, req: HttpServletRequest): ResponseEntity<ErrorBody> {
    log.debug("Database conflict : ", e)

    val cause = e.cause
    if (cause is JDBCException) {
      return toErrorResponse(conflict(buildServiceName(req), cause.sqlException.message, stacktrace(e)))
    }
    return toErrorResponse(conflict(buildServiceName(req), e.message, stacktrace(e)))
  }

  @ExceptionHandler(Throwable::class)
  fun handleException(req: HttpServletRequest, e: Throwable): ResponseEntity<ErrorBody> {
    log.error("Unexpected error : ", e)

    val responseStatus = AnnotationUtils.findAnnotation(e.javaClass, ResponseStatus::class.java)

    val status = responseStatus?.value ?: HttpStatus.INTERNAL_SERVER_ERROR
    val message = responseStatus?.reason ?: getMessageByEnvironment(e)

    return toErrorResponse(defaultError(
      buildServiceName(req), status, "error." + upperCamelToSnakeCase(e.javaClass.simpleName),
      message, stacktrace(e)
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

    return toErrorResponse(badRequest(buildServiceName(req),
      "error.invalid." + lowerCamelToSnakeCase(bindingResult.objectName),
      e.message,
      stacktrace(e),
      errors))
  }

  private fun stacktrace(e: Throwable): String {
    return if (properties.displayFullStacktrace) ExceptionUtils.getStackTrace(e) else ""
  }

  private fun buildServiceName(req: HttpServletRequest): String {
    return "$applicationName : ${req.method} ${req.requestURI}"
  }

  override fun setEnvironment(environment: Environment) {
    this.environment = environment
  }

  companion object {

    private const val DEFAULT_INTERNAL_ERROR_MESSAGE = "An internal error occurred on processing request."
    private const val INVALID_ERROR_PREFIX = "error.invalid"
    private const val MISSING_ERROR_PREFIX = "error.missing"

    private fun toValidationErrorBody(objectError: ObjectError): ValidationErrorBody {
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

    private fun toValidationErrorBody(constraintViolation: ConstraintViolation<*>): ValidationErrorBody {
      val fieldName = constraintViolation.propertyPath?.toString()
      val errorCode = toErrorCode(INVALID_ERROR_PREFIX, fieldName)

      return ValidationErrorBody(
        code = errorCode,
        field = fieldName,
        message = constraintViolation.message
      )
    }

    private fun toErrorCode(errorPrefix: String, fieldName: String?): String {
      val field = if (fieldName.isNullOrBlank()) {
        "unknown"
      } else {
        ".${lowerCamelToSnakeCase(fieldName)}"
      }

      return errorPrefix + field
    }

    private fun lowerCamelToSnakeCase(label: String): String {
      return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, label)
    }

    private fun upperCamelToSnakeCase(label: String): String {
      return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, label)
    }

    private fun toErrorResponse(errorBody: ErrorBody, httpHeaders: HttpHeaders? = null): ResponseEntity<ErrorBody> {
      return ResponseEntity(errorBody, httpHeaders, HttpStatus.valueOf(errorBody.status))
    }
  }
}
