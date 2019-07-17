package com.ekino.oss.errors.handler

import com.ekino.oss.errors.ErrorBody
import com.ekino.oss.errors.generator.notFound
import com.ekino.oss.errors.property.ErrorsProperties
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import javax.servlet.http.HttpServletRequest

/**
 * Handler for Spring MVC exceptions.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
abstract class MvcExceptionHandler(
  private val applicationName: String,
  private val properties: ErrorsProperties
) {
  private val log = LoggerFactory.getLogger(this.javaClass.name)

  @ExceptionHandler(NoHandlerFoundException::class)
  fun handleNoHandlerFoundException(req: HttpServletRequest, e: NoHandlerFoundException): ResponseEntity<ErrorBody> {
    log.trace("Resource not found : ", e)
    return toErrorResponse(notFound(buildServiceName(req, applicationName), e.message, stacktrace(e, properties.displayFullStacktrace)))
  }
}
