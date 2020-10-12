package com.ekino.oss.errors.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("ekino.errors")
public class ErrorsProperties {

  public var displayFullStacktrace: Boolean = false
}
