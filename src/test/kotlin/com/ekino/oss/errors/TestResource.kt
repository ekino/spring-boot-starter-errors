package com.ekino.oss.errors

import org.hibernate.validator.constraints.Length
import org.springframework.beans.InvalidPropertyException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
class TestResource {

  @GetMapping("/ok")
  @Suppress("UnusedPrivateMember")
  fun getOk(@RequestParam id: UUID): ResponseEntity<String> = ResponseEntity.ok("OK")

  @PostMapping("/ok", consumes = [APPLICATION_JSON_VALUE])
  fun postOk(@RequestBody @Valid body: PostBody): ResponseEntity<String> = ResponseEntity.ok(body.message!!)

  @PutMapping("/ok")
  fun putOk(@RequestBody @Valid body: NonNullablePutBody) = ResponseEntity.ok(body.message)

  @GetMapping("$ERROR_PATH/unexpected")
  fun unexpectedError(): ResponseEntity<String> = throw IllegalArgumentException()

  @GetMapping("$ERROR_PATH/custom")
  fun customExceptionError(): ResponseEntity<String> = throw MyException()

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

  @GetMapping("$ERROR_PATH/no-handler-found-error")
  fun noHandlerFoundError(httpServletRequest: HttpServletRequest): ResponseEntity<String> {
    throw NoHandlerFoundException(httpServletRequest.method, httpServletRequest.requestURI, HttpHeaders())
  }

  @PostMapping("/body-with-enum", consumes = [APPLICATION_JSON_VALUE])
  fun postEnum(@RequestBody body: BodyWithEnum): ResponseEntity<BodyWithEnum> = ResponseEntity.ok(body)

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

  data class NonNullablePutBody(
    val message: String
  )

  data class BodyWithEnum(
    val color: Color,
    val myObject: ObjectWithEnum,
    val array: List<Color>
  )

  data class ObjectWithEnum(val color: Color)

  enum class Color {
    RED, BLUE
  }
}
