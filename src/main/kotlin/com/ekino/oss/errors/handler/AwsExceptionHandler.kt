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
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import javax.servlet.http.HttpServletRequest

/**
 * Handler for AWS SDK V2 exceptions.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
abstract class AwsExceptionHandler(
  private val applicationName: String,
  private val properties: ErrorsProperties
) {
  private val log = LoggerFactory.getLogger(this.javaClass.name)

  @ExceptionHandler(NoSuchKeyException::class)
  fun handleNoSuchKeyException(req: HttpServletRequest, e: NoSuchKeyException): ResponseEntity<ErrorBody> {
    log.debug("Object not found on S3", e)
    return toErrorResponse(notFound(buildServiceName(req, applicationName), e.message, e.toStacktrace(properties.displayFullStacktrace)))
  }
}
