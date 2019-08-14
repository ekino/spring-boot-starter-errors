package com.ekino.oss.errors

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

const val RESOLVED_ERROR_PATH = API_PATH + ERROR_PATH

@WebMvcTest(properties = ["ekino.errors.display-full-stacktrace=false", "spring.application.name=myApp"])
@ImportAutoConfiguration(ErrorsAutoConfiguration::class)
class RestExceptionHandlerTest(
  private val mockMvc: MockMvc
) {

  @Test
  fun `should get access denied error`() {
    mockMvc.get("$RESOLVED_ERROR_PATH/accessDenied")
      .andExpect {
        status { isForbidden }
        jsonPath("$.status", `is`(HttpStatus.FORBIDDEN.value()))
        jsonPath("$.code", `is`("error.access_denied"))
        jsonPath("$.message", `is`(HttpStatus.FORBIDDEN.reasonPhrase))
        jsonPath("$.description", `is`(ERROR_MESSAGE))
      }
  }

  @Test
  fun `should get credentials not found error`() {
    mockMvc.get("$RESOLVED_ERROR_PATH/noCredentials")
      .andExpect {
        status { isUnauthorized }
        jsonPath("$.status", `is`(HttpStatus.UNAUTHORIZED.value()))
        jsonPath("$.code", `is`("error.unauthorized"))
        jsonPath("$.message", `is`(HttpStatus.UNAUTHORIZED.reasonPhrase))
        jsonPath("$.description", `is`(ERROR_MESSAGE))
      }
  }

  @Test
  fun `should get insufficient credentials error`() {
    mockMvc.get("$RESOLVED_ERROR_PATH/insufficientCredentials")
      .andExpect {
        status { isUnauthorized }
        jsonPath("$.status", `is`(HttpStatus.UNAUTHORIZED.value()))
        jsonPath("$.code", `is`("error.unauthorized"))
        jsonPath("$.message", `is`(HttpStatus.UNAUTHORIZED.reasonPhrase))
        jsonPath("$.description", `is`(ERROR_MESSAGE))
      }
  }

  @Test
  fun `should get username not found error`() {
    mockMvc.post("$RESOLVED_ERROR_PATH/username-not-found-error") {
      contentType = MediaType.APPLICATION_JSON
      content = "{}"
    }.andExpect {
      status { isUnauthorized }
      jsonPath("$.status", `is`(HttpStatus.UNAUTHORIZED.value()))
      jsonPath("$.code", `is`("error.unauthorized"))
      jsonPath("$.message", `is`(HttpStatus.UNAUTHORIZED.reasonPhrase))
      jsonPath("$.description", `is`(ERROR_MESSAGE))
    }
  }

  @Test
  fun `should get disabled account error`() {
    mockMvc.get("$RESOLVED_ERROR_PATH/disabledAccount")
      .andExpect {
        status { isForbidden }
        jsonPath("$.status", `is`(HttpStatus.FORBIDDEN.value()))
        jsonPath("$.code", `is`("error.disabled_account"))
        jsonPath("$.message", `is`(HttpStatus.FORBIDDEN.reasonPhrase))
        jsonPath("$.description", `is`("Message for developers"))
      }
  }
}
