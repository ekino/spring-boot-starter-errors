package com.ekino.oss.errors.handler

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class HandlerHelperTest {

  @Test
  fun camelCase() {
    assertThat("camelCase".camelToSnakeCase()).isEqualTo("camel_case")
  }

  @Test
  fun simple() {
    assertThat("simple".camelToSnakeCase()).isEqualTo("simple")
  }

  @Test
  fun veryVeryLongCamelCase() {
    assertThat("veryVeryLongCamelCase".camelToSnakeCase()).isEqualTo("very_very_long_camel_case")
  }

  @Test
  fun upperCamelCase() {
    assertThat("UpperCamelCase".camelToSnakeCase()).isEqualTo("upper_camel_case")
  }
}
