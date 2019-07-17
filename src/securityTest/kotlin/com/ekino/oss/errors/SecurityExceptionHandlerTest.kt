package com.ekino.oss.errors

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
  fun should_get_access_denied_error() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/accessDenied"))
      .andExpect(status().isForbidden)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.FORBIDDEN.value())))
      .andExpect(jsonPath("$.code", `is`("error.access_denied")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.FORBIDDEN.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`(ERROR_MESSAGE)))
  }

  @Test
  fun should_get_credentials_not_found_error() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/noCredentials"))
      .andExpect(status().isUnauthorized)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.UNAUTHORIZED.value())))
      .andExpect(jsonPath("$.code", `is`("error.unauthorized")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.UNAUTHORIZED.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`(ERROR_MESSAGE)))
  }

  @Test
  fun should_get_insufficient_credentials_error() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/insufficientCredentials"))
      .andExpect(status().isUnauthorized)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.UNAUTHORIZED.value())))
      .andExpect(jsonPath("$.code", `is`("error.unauthorized")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.UNAUTHORIZED.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`(ERROR_MESSAGE)))
  }

  @Test
  fun should_get_username_not_found_error() {
    mockMvc.perform(post("$RESOLVED_ERROR_PATH/username-not-found-error")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{}"))
      .andExpect(status().isUnauthorized)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.UNAUTHORIZED.value())))
      .andExpect(jsonPath("$.code", `is`("error.unauthorized")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.UNAUTHORIZED.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`(ERROR_MESSAGE)))
  }

  @Test
  fun should_get_disabled_account_error() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/disabledAccount"))
      .andExpect(status().isForbidden)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.FORBIDDEN.value())))
      .andExpect(jsonPath("$.code", `is`("error.disabled_account")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.FORBIDDEN.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`("Message for developers")))
  }
}
