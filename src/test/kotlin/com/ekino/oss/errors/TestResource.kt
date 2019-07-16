package com.ekino.oss.errors

import com.fasterxml.jackson.databind.JsonNode
import org.hibernate.validator.constraints.Length
import org.springframework.beans.InvalidPropertyException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.data.rest.webmvc.ResourceNotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.NoHandlerFoundException
import java.net.ConnectException
import java.util.UUID
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException
import javax.validation.Valid
import javax.validation.Validation
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

const val API_PATH = "/test"
const val ERROR_PATH = "/error"
const val ERROR_MESSAGE = "Message for developers"

@RestController
@RequestMapping(value = [API_PATH], produces = [APPLICATION_JSON_VALUE])
class TestResource(private val validator: Validator) {

  @GetMapping("/ok")
  fun getOk(@RequestParam id: UUID?): ResponseEntity<String> = ResponseEntity.ok("OK")

  @PostMapping("/ok")
  fun postOk(@RequestBody @Valid body: PostBody): ResponseEntity<String> = ResponseEntity.ok(body.message!!)

  @PostMapping("$ERROR_PATH/conflict")
  fun postConflict(@RequestBody body: PostBody): ResponseEntity<String> = throw DataIntegrityViolationException(ERROR_MESSAGE)

  @PostMapping("$ERROR_PATH/username-not-found-error")
  fun getUsernameNotFoundError(@RequestBody body: JsonNode): ResponseEntity<String> = throw UsernameNotFoundException(ERROR_MESSAGE)

  @GetMapping("$ERROR_PATH/no-handler-found-error")
  fun noHandlerFoundError(httpServletRequest: HttpServletRequest): ResponseEntity<String> {
    throw NoHandlerFoundException(httpServletRequest.method, httpServletRequest.requestURI, HttpHeaders())
  }

  @GetMapping("$ERROR_PATH/notFound")
  fun notFoundError(): ResponseEntity<String> = throw ResourceNotFoundException(ERROR_MESSAGE)

  @GetMapping("$ERROR_PATH/accessDenied")
  fun accessDeniedError(): ResponseEntity<String> = throw AccessDeniedException(ERROR_MESSAGE)

  @GetMapping("$ERROR_PATH/disabledAccount")
  fun disabledAccountError(): ResponseEntity<String> = throw DisabledException(ERROR_MESSAGE)

  @GetMapping("$ERROR_PATH/noCredentials")
  fun credentialsNotFoundError(): ResponseEntity<String> = throw AuthenticationCredentialsNotFoundException(ERROR_MESSAGE)

  @GetMapping("$ERROR_PATH/insufficientCredentials")
  fun insufficientCredentialsError(): ResponseEntity<String> = throw InsufficientAuthenticationException(ERROR_MESSAGE)

  @GetMapping("$ERROR_PATH/unexpected")
  fun unexpectedError(): ResponseEntity<String> = throw IllegalArgumentException()

  @GetMapping("$ERROR_PATH/unavailable")
  fun unavailableError(): ResponseEntity<String> = throw ConnectException(ERROR_MESSAGE)

  @GetMapping("$ERROR_PATH/constraint-violation-error")
  fun constraintViolationError(): ResponseEntity<String> {
    val postBody = PostBody(message = "Some message")
    val violations = Validation.buildDefaultValidatorFactory()
      .validator
      .validate(postBody)

    throw ConstraintViolationException("Some violation error message", violations)
  }

  @GetMapping("$ERROR_PATH/nested-constraint-violation-error")
  fun nestedConstraintViolationError(): ResponseEntity<String> {
    val postBody = PostBody(message = "Some message")
    val violations = Validation.buildDefaultValidatorFactory()
      .validator
      .validate(postBody)

    val cause = ConstraintViolationException("Some violation error message", violations)

    throw InvalidPropertyException(PostBody::class.java, "internalBody", "Some root error message...", cause)
  }

  @GetMapping("$ERROR_PATH/repository-constraint-validation")
  fun repositoryConstraintValidationError(): ResponseEntity<String> {
    val postBody = PostBody(message = "Some message")

    val errors = BeanPropertyBindingResult(postBody, "postBody")
    errors.rejectValue("", "error.global", "Some business rules validation failed.")

    validator.validate(postBody, errors)

    throw RepositoryConstraintViolationException(errors)
  }

  data class PostBody(
    @field:Length(min = 3, max = 15)
    @field:NotEmpty
    val message: String? = null,

    @field:Valid
    @field:NotNull
    val internalBody: InternalPostBody? = null
  )

  data class InternalPostBody(
    @field:NotNull
    val value: Int? = null
  )
}
