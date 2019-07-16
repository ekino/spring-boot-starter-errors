package com.ekino.oss.errors

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest

@WebMvcTest
@ImportAutoConfiguration(ErrorsAutoConfiguration::class)
class AutoConfigurationTest {

  @Test
  fun `App can start with ErrorsAutoConfiguration`() {
  }
}
