package com.ekino.oss.errors

import com.ekino.oss.jcv.assertion.hamcrest.JsonMatchers.jsonMatcher
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
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
  fun `should get conflict error`() {
    mockMvc.perform(post("$RESOLVED_ERROR_PATH/conflict")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{\"message\":\"a\", \"internalBody\":{}}"))
      .andExpect(status().isConflict)
      .andExpect(MockMvcResultMatchers.content().string(
        jsonMatcher("""
          {
            "status": 409,
            "code": "error.conflict",
            "message": "Conflict",
            "description": "Message for developers",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : POST /test/error/conflict",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent())
      ))
  }
}
