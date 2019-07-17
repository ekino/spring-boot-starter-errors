package com.ekino.oss.errors

import com.ekino.oss.errors.handler.CoreExceptionHandler
import com.ekino.oss.errors.handler.DataRestExceptionHandler
import com.ekino.oss.errors.handler.MvcExceptionHandler
import com.ekino.oss.errors.handler.SecurityExceptionHandler
import com.ekino.oss.errors.property.ErrorsProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@AutoConfigureBefore(ErrorMvcAutoConfiguration::class)
@EnableConfigurationProperties(ErrorsProperties::class)
class ErrorsAutoConfiguration(
  @param:Value("\${spring.application.name}") private val applicationName: String,
  private val properties: ErrorsProperties
) {

  @Bean
  fun coreExceptionHandler(): CoreExceptionHandler {
    return object : CoreExceptionHandler(applicationName, properties) {}
  }

  @Bean
  @ConditionalOnClass(org.springframework.security.authentication.AuthenticationManager::class)
  fun securityExceptionHandler(): SecurityExceptionHandler {
    return object : SecurityExceptionHandler(applicationName, properties) {}
  }

  @Bean
  @ConditionalOnClass(org.springframework.data.rest.core.config.RepositoryRestConfiguration::class)
  fun dataRestExceptionHandler(): DataRestExceptionHandler {
    return object : DataRestExceptionHandler(applicationName, properties) {}
  }

  @Bean
  @ConditionalOnClass(org.springframework.web.servlet.DispatcherServlet::class)
  fun mvcExceptionHandler(): MvcExceptionHandler {
    return object : MvcExceptionHandler(applicationName, properties) {}
  }
}
