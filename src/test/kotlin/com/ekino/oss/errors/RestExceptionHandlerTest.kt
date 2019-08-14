package com.ekino.oss.errors

import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.emptyOrNullString
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

const val ROOT_PATH = API_PATH
const val RESOLVED_ERROR_PATH = API_PATH + ERROR_PATH

@WebMvcTest(properties = ["ekino.errors.display-full-stacktrace=false", "spring.application.name=myApp"])
@ImportAutoConfiguration(ErrorsAutoConfiguration::class)
class RestExceptionHandlerTest(
  private val mockMvc: MockMvc
) {

  @Test
  fun `should get ok response`() {
    mockMvc.get("$ROOT_PATH/ok")
      .andExpect {
        status { isOk }
      }
  }

  @Test
  fun `should post with validation errors`() {
    mockMvc.post("$ROOT_PATH/ok") {
      contentType = MediaType.APPLICATION_JSON
      content = """{"message":"a", "internalBody":{}}"""
    }.andExpect {
      status { isBadRequest }
      jsonPath("$.status", `is`(HttpStatus.BAD_REQUEST.value()))
      jsonPath("$.code", `is`("error.invalid.post_body"))
      jsonPath("$.message", `is`(HttpStatus.BAD_REQUEST.reasonPhrase))
      jsonPath("$.description", `is`(not(emptyOrNullString())))
      jsonPath("$.errors", hasSize<Any>(2))
      jsonPath("$.errors[?(@.code == 'error.missing.internal_body.value')]" +
        "[?(@.field == 'internalBody.value')]" +
        "[?(@.message == 'must not be null')]").exists()
      jsonPath("$.errors[?(@.code == 'error.invalid.message')]" +
        "[?(@.field == 'message')]" +
        "[?(@.message == 'length must be between 3 and 15')]").exists()
      jsonPath("$.service", `is`("myApp : POST /test/ok"))
    }
  }

  @Test
  fun `should get not readable error`() {
    mockMvc.perform(post("$ROOT_PATH/ok")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{")
    )
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.BAD_REQUEST.value())))
      .andExpect(jsonPath("$.code", `is`("error.not_readable_json")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.BAD_REQUEST.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`(not(emptyOrNullString()))))
  }

  @Test
  fun `should get argument type mismatch error`() {
    mockMvc.perform(get("$ROOT_PATH/ok?id=1234"))
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.BAD_REQUEST.value())))
      .andExpect(jsonPath("$.code", `is`("error.argument_type_mismatch")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.BAD_REQUEST.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`(not(emptyOrNullString()))))
  }

  @Test
  fun `should get method not supported method error`() {
    mockMvc.perform(delete("$ROOT_PATH/ok"))
      .andExpect(status().isMethodNotAllowed)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.METHOD_NOT_ALLOWED.value())))
      .andExpect(jsonPath("$.code", `is`("error.method_not_allowed")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.METHOD_NOT_ALLOWED.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`(not(emptyOrNullString()))))
  }

  @Test
  fun `should get unexpected error()`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/unexpected")).andDo(MockMvcResultHandlers.print())
      .andExpect(status().isInternalServerError)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.INTERNAL_SERVER_ERROR.value())))
      .andExpect(jsonPath("$.code", `is`("error.illegal_argument_exception")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase)))
      .andExpect(jsonPath("$.description", startsWith("An internal error occurred on processing request.")))
      .andExpect(jsonPath("$.stacktrace", `is`("")))
  }

  @Test
  fun `should get unavailable error()`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/unavailable"))
      .andExpect(status().isServiceUnavailable)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.SERVICE_UNAVAILABLE.value())))
      .andExpect(jsonPath("$.code", `is`("error.unavailable")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.SERVICE_UNAVAILABLE.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`(ERROR_MESSAGE)))
  }

  @Test
  fun `should get nested exception error`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/nested-constraint-violation-error")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.BAD_REQUEST.value())))
      .andExpect(jsonPath("$.code", `is`("error.invalid")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.BAD_REQUEST.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`(not(emptyOrNullString()))))
      .andExpect(jsonPath("$.errors", hasSize<Any>(1)))
      .andExpect(jsonPath("$.errors[?(@.code == 'error.invalid.internal_body')]" +
        "[?(@.field == 'internalBody')]" +
        "[?(@.message == 'must not be null')]").exists())
      .andExpect(jsonPath("$.service", `is`("myApp : GET /test/error/nested-constraint-violation-error")))
  }

  @Test
  fun `should get constraint exception error()`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/constraint-violation-error")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.BAD_REQUEST.value())))
      .andExpect(jsonPath("$.code", `is`("error.invalid")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.BAD_REQUEST.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`(not(emptyOrNullString()))))
      .andExpect(jsonPath("$.errors", hasSize<Any>(1)))
      .andExpect(jsonPath("$.errors[?(@.code == 'error.invalid.internal_body')]" +
        "[?(@.field == 'internalBody')]" +
        "[?(@.message == 'must not be null')]").exists())
      .andExpect(jsonPath("$.service", `is`("myApp : GET /test/error/constraint-violation-error")))
  }

  @Test
  fun `should get not found error bis()`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/no-handler-found-error"))
      .andExpect(status().isNotFound)
      .andExpect(jsonPath("$.status", `is`(HttpStatus.NOT_FOUND.value())))
      .andExpect(jsonPath("$.code", `is`("error.not_found")))
      .andExpect(jsonPath("$.message", `is`(HttpStatus.NOT_FOUND.reasonPhrase)))
      .andExpect(jsonPath("$.description", `is`("No handler found for GET /test/error/no-handler-found-error")))
  }
}
