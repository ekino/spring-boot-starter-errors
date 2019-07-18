package com.ekino.oss.errors

import com.ekino.oss.errors.handler.CoreExceptionHandler
import com.ekino.oss.errors.handler.DataRestExceptionHandler
import com.ekino.oss.errors.handler.SecurityExceptionHandler
import com.ekino.oss.errors.handler.TxExceptionHandler
import com.ekino.oss.errors.property.ErrorsProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * We have inner class for some config because using [ConditionalOnClass] on [Bean] methods is not recommended.
 */
@Configuration
@ConditionalOnWebApplication(type = SERVLET)
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

  @Configuration
  @ConditionalOnClass(org.springframework.security.authentication.AuthenticationManager::class)
  class SecurityHandlerConfiguration(
    @param:Value("\${spring.application.name}") private val applicationName: String,
    private val properties: ErrorsProperties
  ) {
    @Bean
    fun securityExceptionHandler(): SecurityExceptionHandler {
      return object : SecurityExceptionHandler(applicationName, properties) {}
    }
  }

  @Configuration
  @ConditionalOnClass(org.springframework.data.rest.core.config.RepositoryRestConfiguration::class)
  class DataRestHandlerConfiguration(
    @param:Value("\${spring.application.name}") private val applicationName: String,
    private val properties: ErrorsProperties
  ) {
    @Bean
    fun dataRestExceptionHandler(): DataRestExceptionHandler {
      return object : DataRestExceptionHandler(applicationName, properties) {}
    }
  }

  @Configuration
  @ConditionalOnClass(org.springframework.dao.DataIntegrityViolationException::class)
  class TxHandlerConfiguration(
    @param:Value("\${spring.application.name}") private val applicationName: String,
    private val properties: ErrorsProperties
  ) {
    @Bean
    fun txExceptionHandler(): TxExceptionHandler {
      return object : TxExceptionHandler(applicationName, properties) {}
    }
  }
}
