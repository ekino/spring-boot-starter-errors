package com.ekino.oss.errors

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.emptyOrNullString
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

const val RESOLVED_ERROR_PATH = API_PATH + ERROR_PATH

@WebMvcTest(properties = ["ekino.errors.display-full-stacktrace=false", "spring.application.name=myApp"])
@ImportAutoConfiguration(ErrorsAutoConfiguration::class)
class RestExceptionHandlerTest(
  private val mockMvc: MockMvc
) {

  @Test
  fun `should get not found error`() {
    mockMvc.get("$RESOLVED_ERROR_PATH/notFound")
      .andExpect {
        status { isNotFound }
        jsonPath("$.status", `is`(HttpStatus.NOT_FOUND.value()))
        jsonPath("$.code", `is`("error.not_found"))
        jsonPath("$.message", `is`(HttpStatus.NOT_FOUND.reasonPhrase))
        jsonPath("$.description", `is`(ERROR_MESSAGE))
      }
  }

  @Test
  fun `should get repository constraint validation error`() {
    mockMvc.get("$RESOLVED_ERROR_PATH/repository-constraint-validation") {
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isBadRequest }
      jsonPath("$.status", `is`(HttpStatus.BAD_REQUEST.value()))
      jsonPath("$.code", `is`("error.invalid"))
      jsonPath("$.message", `is`(HttpStatus.BAD_REQUEST.reasonPhrase))
      jsonPath("$.description", `is`(not(emptyOrNullString())))
      jsonPath("$.errors", hasSize<Any>(1))
      jsonPath("$.errors[?(@.code == 'error.missing.internal_body')]" +
        "[?(@.field == 'internalBody')]" +
        "[?(@.message == 'must not be null')]").exists()
      jsonPath("$.globalErrors", hasSize<Any>(1))
      jsonPath("$.globalErrors[0].code", `is`("error.invalidunknown"))
      jsonPath("$.globalErrors[0].field").doesNotExist()
      jsonPath("$.globalErrors[0].message", `is`("Some business rules validation failed."))
      jsonPath("$.service", `is`("myApp : GET /test/error/repository-constraint-validation"))
    }
  }
}
