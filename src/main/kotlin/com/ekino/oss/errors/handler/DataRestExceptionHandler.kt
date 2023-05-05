package com.ekino.oss.errors.handler

import com.ekino.oss.errors.ErrorBody
import com.ekino.oss.errors.generator.badRequest
import com.ekino.oss.errors.generator.conflict
import com.ekino.oss.errors.generator.notFound
import com.ekino.oss.errors.property.ErrorsProperties
import org.hibernate.JDBCException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.data.rest.webmvc.ResourceNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import jakarta.servlet.http.HttpServletRequest

/**
 * Handler for Spring Data Rest exceptions.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
abstract class DataRestExceptionHandler(
  private val applicationName: String,
  private val properties: ErrorsProperties
) {
  private val log by logger()

  @ExceptionHandler(ResourceNotFoundException::class)
  fun handleResourceNotFoundException(req: HttpServletRequest, e: ResourceNotFoundException): ResponseEntity<ErrorBody> {
    log.trace("Resource not found : ", e)
    return notFound(req.toServiceName(applicationName), e.message, e.toStacktrace(properties.displayFullStacktrace)).toErrorResponse()
  }

  @ExceptionHandler(RepositoryConstraintViolationException::class)
  fun handleRepositoryConstraintViolationException(
    e: RepositoryConstraintViolationException,
    req: HttpServletRequest
  ): ResponseEntity<ErrorBody> {
    log.debug("Constraint violation errors : ", e)

    val errors = e.errors.fieldErrors.map { it.toValidationErrorBody() }
    val globalErrors = e.errors.globalErrors.map { it.toValidationErrorBody() }

    return badRequest(
      req.toServiceName(applicationName),
      INVALID_ERROR_PREFIX,
      e.message,
      e.toStacktrace(properties.displayFullStacktrace),
      errors,
      globalErrors
    ).toErrorResponse()
  }

  @ExceptionHandler(DataIntegrityViolationException::class)
  fun handleConflict(e: DataIntegrityViolationException, req: HttpServletRequest): ResponseEntity<ErrorBody> {
    log.debug("Database conflict : ", e)

    val cause = e.cause
    if (cause is JDBCException) {
      return conflict(
        req.toServiceName(applicationName),
        cause.sqlException.message,
        e.toStacktrace(properties.displayFullStacktrace)
      ).toErrorResponse()
    }
    return conflict(req.toServiceName(applicationName), e.message, e.toStacktrace(properties.displayFullStacktrace)).toErrorResponse()
  }
}
