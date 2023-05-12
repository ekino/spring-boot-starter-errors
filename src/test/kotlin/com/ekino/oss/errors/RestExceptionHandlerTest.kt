package com.ekino.oss.errors

import com.ekino.oss.jcv.assertion.hamcrest.JsonMatchers.jsonMatcher
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

const val ROOT_PATH = API_PATH
const val RESOLVED_ERROR_PATH = API_PATH + ERROR_PATH

@WebMvcTest(properties = ["ekino.errors.display-full-stacktrace=false", "spring.application.name=myApp"])
@ImportAutoConfiguration(ErrorsAutoConfiguration::class)
class RestExceptionHandlerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Test
  fun `should get ok response`() {
    mockMvc.perform(get("$ROOT_PATH/ok?id=${UUID.randomUUID()}"))
      .andExpect(status().isOk)
  }

  @Test
  fun `should get missing parameter error`() {
    mockMvc.perform(get("$ROOT_PATH/ok"))
      .andExpect(status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.content().string(
        jsonMatcher(
          """
          {
            "status": 400,
            "code": "error.missing_parameter",
            "message": "Bad Request",
            "description": "{#not_empty#}",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/ok",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should post with validation errors`() {
    mockMvc.perform(
      post("$ROOT_PATH/ok")
      .contentType(MediaType.APPLICATION_JSON)
      .content("""{"message":"a", "internalBody":{}}""")
    )
      .andExpect(status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.content().string(
        jsonMatcher(
          """
          {
            "status": 400,
            "code": "error.invalid.post_body",
            "message": "Bad Request",
            "description": "The request is invalid",
            "errors": [
              {
                "code": "error.invalid.message",
                "field": "message",
                "message": "length must be between 3 and 15"
              },
              {
                "code": "error.missing.internal_body.value",
                "field": "internalBody.value",
                "message": "must not be null"
              }
            ],
            "globalErrors": [],
            "service": "myApp : POST /test/ok",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should get not readable error`() {
    mockMvc.perform(
      post("$ROOT_PATH/ok")
      .contentType(MediaType.APPLICATION_JSON)
      .content("{")
    )
      .andExpect(status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.content().string(
        jsonMatcher(
          """
          {
            "status": 400,
            "code": "error.not_readable_json",
            "message": "Bad Request",
            "description": "{#not_empty#}",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : POST /test/ok",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should get not readable error if invalid but readable json`() {
    mockMvc.perform(
      put("$ROOT_PATH/ok")
        .contentType(MediaType.APPLICATION_JSON)
        // language=json
        .content("{}")
    )
      .andExpect(status().isBadRequest)
      // language=json
      .andExpect(
        MockMvcResultMatchers.content().string(
          jsonMatcher(
            """
        {
          "status": 400,
          "code": "error.not_readable_json",
          "message": "Bad Request",
          "description": "{#not_empty#}",
          "errors": [],
          "globalErrors": [],
          "service": "myApp : PUT /test/ok",
          "stacktrace": "",
          "timestamp": "{#date_time_format:iso_instant#}"
        }
      """.trimIndent()
          )
        )
      )
  }

  @Test
  fun `should get argument type mismatch error`() {
    mockMvc.perform(get("$ROOT_PATH/ok?id=1234"))
      .andExpect(status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.content().string(
        jsonMatcher(
          """
          {
            "status": 400,
            "code": "error.argument_type_mismatch",
            "message": "Bad Request",
            "description": "{#not_empty#}",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/ok",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should get method not supported method error`() {
    mockMvc.perform(delete("$ROOT_PATH/ok"))
      .andExpect(status().isMethodNotAllowed)
      .andExpect(
        MockMvcResultMatchers.content().string(
        jsonMatcher(
          """
          {
            "status": 405,
            "code": "error.method_not_allowed",
            "message": "Method Not Allowed",
            "description": "{#not_empty#}",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : DELETE /test/ok",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should get unexpected error`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/unexpected"))
      .andExpect(status().isInternalServerError)
      .andExpect(
        MockMvcResultMatchers.content().string(
        jsonMatcher(
          """
          {
            "status": 500,
            "code": "error.illegal_argument_exception",
            "message": "Internal Server Error",
            "description": "An internal error occurred on processing request.",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/error/unexpected",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should get custom exception error`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/custom"))
      .andExpect(status().isGone)
      .andExpect(
        MockMvcResultMatchers.content().string(
        jsonMatcher(
          """
          {
            "status": 410,
            "code": "error.my_exception",
            "message": "Gone",
            "description": "",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/error/custom",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should get unavailable error`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/unavailable"))
      .andExpect(status().isServiceUnavailable)
      .andExpect(
        MockMvcResultMatchers.content().string(
        jsonMatcher(
          """
          {
            "status": 503,
            "code": "error.unavailable",
            "message": "Service Unavailable",
            "description": "Message for developers",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/error/unavailable",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should get nested exception error`() {
    mockMvc.perform(
      get("$RESOLVED_ERROR_PATH/nested-constraint-violation-error")
      .accept(MediaType.APPLICATION_JSON)
    )
      .andExpect(status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.content().string(
        jsonMatcher(
          """
          {
            "status": 400,
            "code": "error.invalid",
            "message": "Bad Request",
            "description": "{#not_empty#}",
            "errors": [
              {
                "code": "error.invalid.internal_body",
                "field": "internalBody",
                "message": "must not be null"
              }
            ],
            "globalErrors": [],
            "service": "myApp : GET /test/error/nested-constraint-violation-error",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should get constraint exception error`() {
    mockMvc.perform(
      get("$RESOLVED_ERROR_PATH/constraint-violation-error")
      .accept(MediaType.APPLICATION_JSON)
    )
      .andExpect(status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.content().string(
        jsonMatcher(
          """
          {
            "status": 400,
            "code": "error.invalid",
            "message": "Bad Request",
            "description": "{#not_empty#}",
            "errors": [
              {
                "code": "error.invalid.internal_body",
                "field": "internalBody",
                "message": "must not be null"
              }
            ],
            "globalErrors": [],
            "service": "myApp : GET /test/error/constraint-violation-error",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should get not found error bis`() {
    mockMvc.perform(get("$RESOLVED_ERROR_PATH/no-handler-found-error"))
      .andExpect(status().isNotFound)
      .andExpect(
        MockMvcResultMatchers.content().string(
        jsonMatcher(
          """
          {
            "status": 404,
            "code": "error.not_found",
            "message": "Not Found",
            "description": "No endpoint GET /test/error/no-handler-found-error.",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : GET /test/error/no-handler-found-error",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should post with not supported media type`() {
    mockMvc.perform(
      post("$ROOT_PATH/ok")
      .contentType(MediaType.APPLICATION_PDF)
    )
      .andExpect(status().isUnsupportedMediaType)
      .andExpect(
        MockMvcResultMatchers.content().string(
        jsonMatcher(
          """
          {
            "status": 415,
            "code": "error.media_type_not_supported",
            "message": "Unsupported Media Type",
            "description": "Content-Type 'application/pdf' is not supported",
            "errors": [],
            "globalErrors": [],
            "service": "myApp : POST /test/ok",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should post with unknown enum value in field`() {
    mockMvc.perform(
      post("$ROOT_PATH/body-with-enum")
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      // language=json
      .content(
        """
        {
          "color": "DOG"
        }
      """.trimIndent()
      )
    )
      .andExpect(status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.content().string(
        // language=json
        jsonMatcher(
          """
          {
            "status": 400,
            "code": "error.invalid.enum",
            "message": "Bad Request",
            "description": "The value 'DOG' is not an accepted value",
            "errors": [
              {
                "code": "error.invalid.color",
                "field": "color",
                "message": "must be one of [RED,BLUE]"
              }
            ],
            "globalErrors": [],
            "service": "myApp : POST /test/body-with-enum",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should post with unknown enum value in object`() {
    mockMvc.perform(
      post("$ROOT_PATH/body-with-enum")
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      // language=json
      .content(
        """
        {
          "myObject": {
            "color": "DOG"
          }
        }
      """.trimIndent()
      )
    )
      .andExpect(status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.content().string(
        // language=json
        jsonMatcher(
          """
          {
            "status": 400,
            "code": "error.invalid.enum",
            "message": "Bad Request",
            "description": "The value 'DOG' is not an accepted value",
            "errors": [
              {
                "code": "error.invalid.my_object.color",
                "field": "myObject.color",
                "message": "must be one of [RED,BLUE]"
              }
            ],
            "globalErrors": [],
            "service": "myApp : POST /test/body-with-enum",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should post with unknown enum value in array`() {
    mockMvc.perform(
      post("$ROOT_PATH/body-with-enum")
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      // language=json
      .content(
        """
        {
          "array": ["BLUE", "DOG", "CAT"]
        }
      """.trimIndent()
      )
    )
      .andExpect(status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.content().string(
        // language=json
        jsonMatcher(
          """
          {
            "status": 400,
            "code": "error.invalid.enum",
            "message": "Bad Request",
            "description": "The value 'DOG' is not an accepted value",
            "errors": [
              {
                "code": "error.invalid.array",
                "field": "array",
                "message": "must be one of [RED,BLUE]"
              }
            ],
            "globalErrors": [],
            "service": "myApp : POST /test/body-with-enum",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should get with unknown enum param`() {
    mockMvc.perform(
      get("$ROOT_PATH/param-enum")
      .contentType(MediaType.APPLICATION_JSON_VALUE)
      .param("color", "DOG")
    )
      .andExpect(status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.content().string(
        // language=json
        jsonMatcher(
          """
          {
            "status": 400,
            "code": "error.invalid.enum",
            "message": "Bad Request",
            "description": "The value 'DOG' is not an accepted value",
            "errors": [
              {
                "code": "error.invalid.param.color",
                "field": "color",
                "message": "must be one of [RED,BLUE]"
              }
            ],
            "globalErrors": [],
            "service": "myApp : GET /test/param-enum",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }

  @Test
  fun `should get with unknown enum path`() {
    mockMvc.perform(
      get("$ROOT_PATH/colors/DOG")
      .contentType(MediaType.APPLICATION_JSON_VALUE)
    )
      .andExpect(status().isBadRequest)
      .andExpect(
        MockMvcResultMatchers.content().string(
        // language=json
        jsonMatcher(
          """
          {
            "status": 400,
            "code": "error.invalid.enum",
            "message": "Bad Request",
            "description": "The value 'DOG' is not an accepted value",
            "errors": [
              {
                "code": "error.invalid.path.color",
                "field": "color",
                "message": "must be one of [RED,BLUE]"
              }
            ],
            "globalErrors": [],
            "service": "myApp : GET /test/colors/DOG",
            "stacktrace": "",
            "timestamp": "{#date_time_format:iso_instant#}"
          }
        """.trimIndent()
        )
      )
      )
  }
}
