package com.ekino.oss.errors

import org.hibernate.validator.constraints.Length
import org.springframework.data.rest.core.RepositoryConstraintViolationException
import org.springframework.data.rest.webmvc.ResourceNotFoundException
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

const val API_PATH = "/test"
const val ERROR_PATH = "/error"
const val ERROR_MESSAGE = "Message for developers"

@RestController
@RequestMapping(value = [API_PATH], produces = [APPLICATION_JSON_VALUE])
class TestResource(private val validator: Validator) {

  @GetMapping("$ERROR_PATH/notFound")
  fun notFoundError(): ResponseEntity<String> = throw ResourceNotFoundException(ERROR_MESSAGE)

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
