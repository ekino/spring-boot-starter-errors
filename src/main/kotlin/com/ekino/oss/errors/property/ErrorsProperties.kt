package com.ekino.oss.errors.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("ekino.errors")
class ErrorsProperties {

  var displayFullStacktrace: Boolean = false
}
