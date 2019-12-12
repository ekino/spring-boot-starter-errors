package com.ekino.oss.errors

import com.ekino.oss.jcv.assertion.hamcrest.JsonMatchers.jsonMatcher
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

const val RESOLVED_ERROR_PATH = API_PATH + ERROR_PATH

@WebMvcTest(properties = ["ekino.errors.display-full-stacktrace=false", "spring.application.name=myApp"])
@ImportAutoConfiguration(ErrorsAutoConfiguration::class)
class RestExceptionHandlerTest(
  private val mockMvc: MockMvc
) {

  @Test
  fun should_get_not_found_error() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/notFound"))
      .andExpect(status().isNotFound)
      .andExpect(MockMvcResultMatchers.content().string(
        jsonMatcher("""
          {
            "status": 404,
            "code": "error.not_found",
            "message": "Not Found",
            "description": "Message for developers",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/error/notFound",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent())
      ))
  }

  @Test
  fun should_get_repository_constraint_validation_error() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/repository-constraint-validation")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest)
      .andExpect(MockMvcResultMatchers.content().string(
        jsonMatcher("""
          {
            "status": 400,
            "code": "error.invalid",
            "message": "Bad Request",
            "description": "{#not_empty#}",
            "errors": [
              {
                "code": "error.missing.internal_body",
                "field": "internalBody",
                "message": "must not be null"
              }
            ],
            "globalErrors": [
              {
                "code": "error.invalidunknown",
                "field": null,
                "message": "Some business rules validation failed."
              }
            ],
            "service": "myApp : GET /test/error/repository-constraint-validation",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent())
      ))
  }
}
