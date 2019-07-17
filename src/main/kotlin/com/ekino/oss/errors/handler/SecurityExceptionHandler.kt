package com.ekino.oss.errors.handler

import com.ekino.oss.errors.ErrorBody
import com.ekino.oss.errors.generator.forbidden
import com.ekino.oss.errors.generator.unAuthorized
import com.ekino.oss.errors.property.ErrorsProperties
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.servlet.http.HttpServletRequest

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
abstract class SecurityExceptionHandler(
  private val applicationName: String,
  private val properties: ErrorsProperties
) {
  private val log = LoggerFactory.getLogger(this.javaClass.name)

  @ExceptionHandler(AuthenticationCredentialsNotFoundException::class, InsufficientAuthenticationException::class, UsernameNotFoundException::class)
  fun handleAuthenticationException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.debug("Authentication failed : ", e)
    return toErrorResponse(unAuthorized(
      buildServiceName(req, applicationName), "error.unauthorized", e.message, stacktrace(e, properties.displayFullStacktrace)
    ))
  }

  @ExceptionHandler(AccessDeniedException::class, BadCredentialsException::class)
  fun handleAccessDeniedException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.debug("Access denied", e)
    return toErrorResponse(forbidden(
      buildServiceName(req, applicationName), "error.access_denied", e.message, stacktrace(e, properties.displayFullStacktrace)
    ))
  }

  @ExceptionHandler(DisabledException::class)
  fun handleDisabledException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.debug("Disable account : ", e)
    return toErrorResponse(forbidden(
      buildServiceName(req, applicationName), "error.disabled_account", e.message, stacktrace(e, properties.displayFullStacktrace)
    ))
  }
}
