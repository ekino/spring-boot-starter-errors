package com.ekino.oss.errors

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

const val RESOLVED_ERROR_PATH = API_PATH + ERROR_PATH

@WebMvcTest(properties = ["ekino.errors.display-full-stacktrace=false", "spring.application.name=myApp"])
@ImportAutoConfiguration(ErrorsAutoConfiguration::class)
class RestExceptionHandlerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Test
  fun should_get_conflict_error() {
    mockMvc.perform(post("$RESOLVED_ERROR_PATH/conflict")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{\"message\":\"a\", \"internalBody\":{}}"))
      .andExpect(status().isConflict)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.CONFLICT.value())))
      .andExpect(jsonPath("$.code", `is`("error.conflict")))
      .andExpect(jsonPath("$.message", `is`("Conflict")))
      .andExpect(jsonPath("$.errors", hasSize<Any>(0)))
  }
}
