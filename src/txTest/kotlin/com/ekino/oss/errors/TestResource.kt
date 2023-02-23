package com.ekino.oss.errors

import org.hibernate.validator.constraints.Length
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

const val API_PATH = "/test"
const val ERROR_PATH = "/error"
const val ERROR_MESSAGE = "Message for developers"

@RestController
@RequestMapping(value = [API_PATH], produces = [APPLICATION_JSON_VALUE])
class TestResource {

  @PostMapping("$ERROR_PATH/conflict")
  fun postConflict(@RequestBody body: PostBody): ResponseEntity<String> = throw DataIntegrityViolationException(ERROR_MESSAGE)

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
