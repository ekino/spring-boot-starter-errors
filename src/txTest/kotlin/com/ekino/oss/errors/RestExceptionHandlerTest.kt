package com.ekino.oss.errors

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.empty
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

const val RESOLVED_ERROR_PATH = API_PATH + ERROR_PATH

@WebMvcTest(properties = ["ekino.errors.display-full-stacktrace=false", "spring.application.name=myApp"])
@ImportAutoConfiguration(ErrorsAutoConfiguration::class)
class RestExceptionHandlerTest(
  private val mockMvc: MockMvc
) {

  @Test
  fun `should get conflict error`() {
    mockMvc.post("$RESOLVED_ERROR_PATH/conflict") {
      contentType = MediaType.APPLICATION_JSON
      content = """{"message":"a", "internalBody":{}}"""
    }.andExpect {
      status { isConflict }
      jsonPath("$.status", `is`(HttpStatus.CONFLICT.value()))
      jsonPath("$.code", `is`("error.conflict"))
      jsonPath("$.message", `is`("Conflict"))
      jsonPath("$.errors", `is`(empty<Any>()))
    }
  }
}
