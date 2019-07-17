package com.ekino.oss.errors

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val API_PATH = "/test"
const val ERROR_PATH = "/error"
const val ERROR_MESSAGE = "Message for developers"

@RestController
@RequestMapping(value = [API_PATH], produces = [APPLICATION_JSON_VALUE])
class TestResource {

  @PostMapping("$ERROR_PATH/username-not-found-error")
  fun getUsernameNotFoundError(@RequestBody body: JsonNode): ResponseEntity<String> = throw UsernameNotFoundException(ERROR_MESSAGE)

  @GetMapping("$ERROR_PATH/accessDenied")
  fun accessDeniedError(): ResponseEntity<String> = throw AccessDeniedException(ERROR_MESSAGE)

  @GetMapping("$ERROR_PATH/disabledAccount")
  fun disabledAccountError(): ResponseEntity<String> = throw DisabledException(ERROR_MESSAGE)

  @GetMapping("$ERROR_PATH/noCredentials")
  fun credentialsNotFoundError(): ResponseEntity<String> = throw AuthenticationCredentialsNotFoundException(ERROR_MESSAGE)

  @GetMapping("$ERROR_PATH/insufficientCredentials")
  fun insufficientCredentialsError(): ResponseEntity<String> = throw InsufficientAuthenticationException(ERROR_MESSAGE)
}
