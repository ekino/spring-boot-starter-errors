package com.ekino.oss.errors

import com.ekino.oss.jcv.assertion.hamcrest.JsonMatchers.jsonMatcher
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

const val RESOLVED_ERROR_PATH = API_PATH + ERROR_PATH

@WebMvcTest(properties = ["ekino.errors.display-full-stacktrace=false", "spring.application.name=myApp"])
@ImportAutoConfiguration(ErrorsAutoConfiguration::class)
class RestExceptionHandlerTest(
  private val mockMvc: MockMvc
) {

  @Test
  fun should_get_no_such_key_exception() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/no-such-key-exception"))
      .andExpect(status().isNotFound)
      .andExpect(content().string(
        jsonMatcher("""
          {
            "status": 404,
            "code": "error.not_found",
            "message": "Not Found",
            "description": "Message for developers",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/error/no-such-key-exception",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent())
      ))
  }
}
