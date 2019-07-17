package com.ekino.oss.errors.handler

import com.ekino.oss.errors.ErrorBody
import com.ekino.oss.errors.generator.badRequest
import com.ekino.oss.errors.generator.conflict
import com.ekino.oss.errors.generator.notFound
import com.ekino.oss.errors.property.ErrorsProperties
import org.hibernate.JDBCException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.data.rest.webmvc.ResourceNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.servlet.http.HttpServletRequest

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
abstract class DataRestExceptionHandler(
  private val applicationName: String,
  private val properties: ErrorsProperties
) {
  private val log = LoggerFactory.getLogger(this.javaClass.name)

  @ExceptionHandler(ResourceNotFoundException::class)
  fun handleResourceNotFoundException(req: HttpServletRequest, e: ResourceNotFoundException): ResponseEntity<ErrorBody> {
    log.trace("Resource not found : ", e)
    return toErrorResponse(notFound(buildServiceName(req, applicationName), e.message, stacktrace(e, properties.displayFullStacktrace)))
  }

  @ExceptionHandler(RepositoryConstraintViolationException::class)
  fun handleRepositoryConstraintViolationException(
    e: RepositoryConstraintViolationException,
    req: HttpServletRequest
  ): ResponseEntity<ErrorBody> {

    log.debug("Constraint violation errors : ", e)

    val errors = e.errors.fieldErrors.map { toValidationErrorBody(it) }
    val globalErrors = e.errors.globalErrors.map { toValidationErrorBody(it) }

    return toErrorResponse(badRequest(
      buildServiceName(req, applicationName), INVALID_ERROR_PREFIX, e.message, stacktrace(e, properties.displayFullStacktrace), errors, globalErrors
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
}
