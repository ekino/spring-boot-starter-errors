package com.ekino.oss.errors.generator

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.ekino.oss.errors.ValidationErrorBody
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ErrorBodyGeneratorTest {

  @Test
  fun should_get_not_found_error_body() {
    val errorCode = "error.not_found"
    val errorDescription = "Not found resource"

    val errorBody = notFound("service_name", errorDescription, "stacktrace")

    assertThat(errorBody.status).isEqualTo(HttpStatus.NOT_FOUND.value())
    assertThat(errorBody.code).isEqualTo(errorCode)
    assertThat(errorBody.message).isEqualTo(HttpStatus.NOT_FOUND.reasonPhrase)
    assertThat(errorBody.description).isEqualTo(errorDescription)
    assertThat(errorBody.errors).isEmpty()
  }

  @Test
  fun should_get_bad_request_error_body() {
    val errorCode = "error.invalid.member"
    val errorDescription = "This is invalid"
    val validationError = ValidationErrorBody(
      code = "error.missing.member.first_name",
      field = "firstName",
      message = "may not be empty"
    )
    val errors = listOf(validationError)

    val errorBody = badRequest("service_name", errorCode, errorDescription, "stacktrace", errors)

    assertThat(errorBody.status).isEqualTo(HttpStatus.BAD_REQUEST.value())
    assertThat(errorBody.code).isEqualTo(errorCode)
    assertThat(errorBody.message).isEqualTo(HttpStatus.BAD_REQUEST.reasonPhrase)
    assertThat(errorBody.description).isEqualTo(errorDescription)
    assertThat(errorBody.errors).hasSize(1)
    assertThat(errorBody.errors[0]).isEqualTo(validationError)
  }

  @Test
  fun should_get_method_not_allowed_error_body() {
    val errorCode = "error.method_not_allowed"
    val errorDescription = "Request method 'DELETE' not supported"

    val errorBody = methodNotAllowed("service_name", errorCode, errorDescription, "stacktrace")

    assertThat(errorBody.status).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value())
    assertThat(errorBody.code).isEqualTo(errorCode)
    assertThat(errorBody.message).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.reasonPhrase)
    assertThat(errorBody.description).isEqualTo(errorDescription)
    assertThat(errorBody.errors).isEmpty()
  }

  @Test
  fun should_get_default_error_body() {
    val errorCode = "error.illegal_argument_exception"
    val errorDescription = "Illegal Argument Exception"

    val errorBody = defaultError("service_name", HttpStatus.INTERNAL_SERVER_ERROR, errorCode, errorDescription, "stacktrace")

    assertThat(errorBody.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
    assertThat(errorBody.code).isEqualTo(errorCode)
    assertThat(errorBody.message).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase)
    assertThat(errorBody.description).isEqualTo(errorDescription)
    assertThat(errorBody.errors).isEmpty()
  }

  @Test
  fun should_get_access_denied_error_body() {
    val errorCode = "error.access_denied"
    val errorDescription = "Access is forbidden"

    val errorBody = forbidden("service_name", errorCode, errorDescription, "stacktrace")

    assertThat(errorBody.status).isEqualTo(HttpStatus.FORBIDDEN.value())
    assertThat(errorBody.code).isEqualTo(errorCode)
    assertThat(errorBody.message).isEqualTo(HttpStatus.FORBIDDEN.reasonPhrase)
    assertThat(errorBody.description).isEqualTo(errorDescription)
    assertThat(errorBody.errors).isEmpty()
  }

  @Test
  fun should_get_credentials_not_found_error_body() {
    val errorCode = "error.unauthorized"
    val errorDescription = "Access is unauthorized"

    val errorBody = unAuthorized("service_name", errorCode, errorDescription, "stacktrace")

    assertThat(errorBody.status).isEqualTo(HttpStatus.UNAUTHORIZED.value())
    assertThat(errorBody.code).isEqualTo(errorCode)
    assertThat(errorBody.message).isEqualTo(HttpStatus.UNAUTHORIZED.reasonPhrase)
    assertThat(errorBody.description).isEqualTo(errorDescription)
    assertThat(errorBody.errors).isEmpty()
  }

  @Test
  fun should_get_unavailable_error_body() {
    val errorCode = "error.unavailable"
    val errorDescription = "Service is unavailable"

    val errorBody = unavailable("service_name", errorCode, errorDescription, "stacktrace")

    assertThat(errorBody.status).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value())
    assertThat(errorBody.code).isEqualTo(errorCode)
    assertThat(errorBody.message).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.reasonPhrase)
    assertThat(errorBody.description).isEqualTo(errorDescription)
    assertThat(errorBody.errors).isEmpty()
  }

  @Test
  fun should_get_conflict_error_body() {
    val errorCode = "error.conflict"
    val errorMessage = "There is a conflict"

    val validationError = ValidationErrorBody(
      field = "firstName",
      code = "error.first_name.conflict",
      message = "There can not be two names"
    )

    val errors = listOf(validationError)

    val errorBody = conflict("service_name", errorMessage, "stacktrace", errors)

    assertThat(errorBody.status).isEqualTo(HttpStatus.CONFLICT.value())
    assertThat(errorBody.code).isEqualTo(errorCode)
    assertThat(errorBody.message).isEqualTo("Conflict")
    assertThat(errorBody.errors[0]).isEqualTo(validationError)
  }

  @Test
  fun should_get_unprocessable_entity_error_body() {
    val errorCode = "error.unprocessable_entity.member"
    val errorDescription = "Could not process the member"

    val validationError = ValidationErrorBody(
      field = "firstName",
      code = "error.first_name.invalid",
      message = "The first name is not valid"
    )

    val errors = listOf(validationError)

    val errorBody = unprocessableEntity("service_name", errorCode, errorDescription, "stacktrace", errors)

    assertThat(errorBody.status).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value())
    assertThat(errorBody.code).isEqualTo(errorCode)
    assertThat(errorBody.message).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.reasonPhrase)
    assertThat(errorBody.description).isEqualTo(errorDescription)
    assertThat(errorBody.errors[0]).isEqualTo(validationError)
  }
}
