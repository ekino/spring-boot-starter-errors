package com.ekino.oss.errors

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

const val RESOLVED_ERROR_PATH = API_PATH + ERROR_PATH

@WebMvcTest(properties = ["ekino.errors.display-full-stacktrace=false", "spring.application.name=myApp"])
@ImportAutoConfiguration(ErrorsAutoConfiguration::class)
class RestExceptionHandlerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Test
  fun should_get_no_such_key_exception() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/no-such-key-exception"))
      .andExpect(status().isNotFound)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.NOT_FOUND.value())))
      .andExpect(jsonPath("$.code", `is`("error.not_found")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.NOT_FOUND.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`("Message for developers")))
  }
}
