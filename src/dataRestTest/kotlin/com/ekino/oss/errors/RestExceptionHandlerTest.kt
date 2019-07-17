package com.ekino.oss.errors

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.isEmptyOrNullString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
  fun should_get_not_found_error() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/notFound"))
      .andExpect(status().isNotFound)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.NOT_FOUND.value())))
      .andExpect(jsonPath("$.code", `is`("error.not_found")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.NOT_FOUND.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`(ERROR_MESSAGE)))
  }

  @Test
  fun should_get_repository_constraint_validation_error() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/repository-constraint-validation")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.BAD_REQUEST.value())))
      .andExpect(jsonPath("$.code", `is`("error.invalid")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.BAD_REQUEST.reasonPhrase)))
      .andExpect(jsonPath("$.description", not(isEmptyOrNullString())))
      .andExpect(jsonPath("$.errors", hasSize<Any>(1)))
      .andExpect(jsonPath("$.errors[?(@.code == 'error.missing.internal_body')]" +
        "[?(@.field == 'internalBody')]" +
        "[?(@.message == 'must not be null')]").exists())
      .andExpect(jsonPath("$.globalErrors", hasSize<Any>(1)))
      .andExpect(jsonPath("$.globalErrors[0].code", `is`("error.invalidunknown")))
      .andExpect(jsonPath("$.globalErrors[0].field").doesNotExist())
      .andExpect(jsonPath("$.globalErrors[0].message", `is`("Some business rules validation failed.")))
      .andExpect(jsonPath("$.service", `is`("myApp : GET /test/error/repository-constraint-validation")))
  }
}
