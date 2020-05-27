package com.ekino.oss.errors

import com.ekino.oss.jcv.assertion.hamcrest.JsonMatchers.jsonMatcher
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

const val RESOLVED_ERROR_PATH = API_PATH + ERROR_PATH

@WebMvcTest(properties = ["ekino.errors.display-full-stacktrace=false", "spring.application.name=myApp"])
@ImportAutoConfiguration(ErrorsAutoConfiguration::class)
class RestExceptionHandlerTest(
  private val mockMvc: MockMvc
) {

  @Test
  fun `should get access denied error`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/accessDenied"))
      .andExpect(status().isForbidden)
      .andExpect(MockMvcResultMatchers.content().string(
        jsonMatcher("""
          {
            "status": 403,
            "code": "error.access_denied",
            "message": "Forbidden",
            "description": "Message for developers",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/error/accessDenied",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent())
      ))
  }

  @Test
  fun `should get credentials not found error`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/noCredentials"))
      .andExpect(status().isUnauthorized)
      .andExpect(MockMvcResultMatchers.content().string(
        jsonMatcher("""
          {
            "status": 401,
            "code": "error.unauthorized",
            "message": "Unauthorized",
            "description": "Message for developers",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/error/noCredentials",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent())
      ))
  }

  @Test
  fun `should get insufficient credentials error`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/insufficientCredentials"))
      .andExpect(status().isUnauthorized)
      .andExpect(MockMvcResultMatchers.content().string(
        jsonMatcher("""
          {
            "status": 401,
            "code": "error.unauthorized",
            "message": "Unauthorized",
            "description": "Message for developers",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/error/insufficientCredentials",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent())
      ))
  }

  @Test
  fun `should get username not found error`() {
    mockMvc.perform(post("$RESOLVED_ERROR_PATH/username-not-found-error")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{}"))
      .andExpect(status().isUnauthorized)
      .andExpect(MockMvcResultMatchers.content().string(
        jsonMatcher("""
          {
            "status": 401,
            "code": "error.unauthorized",
            "message": "Unauthorized",
            "description": "Message for developers",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : POST /test/error/username-not-found-error",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent())
      ))
  }

  @Test
  fun `should get disabled account error`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/disabledAccount"))
      .andExpect(status().isForbidden)
      .andExpect(MockMvcResultMatchers.content().string(
        jsonMatcher("""
          {
            "status": 403,
            "code": "error.disabled_account",
            "message": "Forbidden",
            "description": "Message for developers",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/error/disabledAccount",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent())
      ))
  }

  @Test
  fun `should get request rejected error`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/requestRejected"))
      .andExpect(status().isForbidden)
      .andExpect(MockMvcResultMatchers.content().string(
        jsonMatcher("""
          {
            "status": 403,
            "code": "error.request_rejected",
            "message": "Forbidden",
            "description": "Message for developers",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/error/requestRejected",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent())
      ))
  }

  @Test
  fun `should get bad credentials error`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/badCredentials"))
      .andExpect(status().isUnauthorized)
      .andExpect(MockMvcResultMatchers.content().string(
        jsonMatcher("""
          {
            "status": 401,
            "code": "error.unauthorized",
            "message": "Unauthorized",
            "description": "Message for developers",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/error/badCredentials",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """)
      ))
  }
}
