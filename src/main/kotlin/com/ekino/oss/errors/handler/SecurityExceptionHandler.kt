package com.ekino.oss.errors.handler

import com.ekino.oss.errors.ErrorBody
import com.ekino.oss.errors.generator.forbidden
import com.ekino.oss.errors.generator.unAuthorized
import com.ekino.oss.errors.property.ErrorsProperties
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.firewall.RequestRejectedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.servlet.http.HttpServletRequest

/**
 * Handler for Spring Security exceptions.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
abstract class SecurityExceptionHandler(
  private val applicationName: String,
  private val properties: ErrorsProperties
) {
  private val log by logger()

  @ExceptionHandler(AuthenticationCredentialsNotFoundException::class, InsufficientAuthenticationException::class, UsernameNotFoundException::class)
  fun handleAuthenticationException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.debug("Authentication failed : ", e)
    return unAuthorized(
      req.toServiceName(applicationName), "error.unauthorized", e.message, e.toStacktrace(properties.displayFullStacktrace)
    ).toErrorResponse()
  }

  @ExceptionHandler(AccessDeniedException::class, BadCredentialsException::class)
  fun handleAccessDeniedException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.debug("Access denied", e)
    return forbidden(
      req.toServiceName(applicationName), "error.access_denied", e.message, e.toStacktrace(properties.displayFullStacktrace)
    ).toErrorResponse()
  }

  @ExceptionHandler(DisabledException::class)
  fun handleDisabledException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.debug("Disable account : ", e)
    return forbidden(
      req.toServiceName(applicationName), "error.disabled_account", e.message, e.toStacktrace(properties.displayFullStacktrace)
    ).toErrorResponse()
  }

  @ExceptionHandler(RequestRejectedException::class)
  fun handleFirewallException(req: HttpServletRequest, e: Exception): ResponseEntity<ErrorBody> {
    log.debug("Access denied", e)
    return forbidden(
      req.toServiceName(applicationName), "error.request_rejected", e.message, e.toStacktrace(properties.displayFullStacktrace)
    ).toErrorResponse()
  }
}
