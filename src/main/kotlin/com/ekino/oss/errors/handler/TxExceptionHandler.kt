package com.ekino.oss.errors.handler

import com.ekino.oss.errors.ErrorBody
import com.ekino.oss.errors.generator.conflict
import com.ekino.oss.errors.property.ErrorsProperties
import org.hibernate.JDBCException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.servlet.http.HttpServletRequest

/**
 * Handler for Spring TX exceptions.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
abstract class TxExceptionHandler(
  private val applicationName: String,
  private val properties: ErrorsProperties
) {
  private val log = LoggerFactory.getLogger(this.javaClass.name)

  @ExceptionHandler(DataIntegrityViolationException::class)
  fun handleConflict(e: DataIntegrityViolationException, req: HttpServletRequest): ResponseEntity<ErrorBody> {
    log.debug("Database conflict : ", e)

    val cause = e.cause
    if (cause is JDBCException) {
      return toErrorResponse(conflict(
        buildServiceName(req, applicationName), cause.sqlException.message, e.toStacktrace(properties.displayFullStacktrace)
      ))
    }
    return toErrorResponse(conflict(buildServiceName(req, applicationName), e.message, e.toStacktrace(properties.displayFullStacktrace)))
  }
}
