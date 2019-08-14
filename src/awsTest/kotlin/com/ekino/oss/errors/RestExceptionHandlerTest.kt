package com.ekino.oss.errors

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

const val RESOLVED_ERROR_PATH = API_PATH + ERROR_PATH

@WebMvcTest(properties = ["ekino.errors.display-full-stacktrace=false", "spring.application.name=myApp"])
@ImportAutoConfiguration(ErrorsAutoConfiguration::class)
class RestExceptionHandlerTest(
  private val mockMvc: MockMvc
) {

  @Test
  fun `should get no such key exception`() {
    mockMvc.get("$RESOLVED_ERROR_PATH/no-such-key-exception")
      .andExpect {
        status { isNotFound }
        jsonPath("$.status", `is`(HttpStatus.NOT_FOUND.value()))
        jsonPath("$.code", `is`("error.not_found"))
        jsonPath("$.message", `is`(HttpStatus.NOT_FOUND.reasonPhrase))
        jsonPath("$.description", `is`("Message for developers"))
      }
  }
}
