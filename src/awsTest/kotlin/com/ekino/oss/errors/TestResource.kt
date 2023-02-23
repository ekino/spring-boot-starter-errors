package com.ekino.oss.errors

import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import jakarta.servlet.http.HttpServletRequest

const val API_PATH = "/test"
const val ERROR_PATH = "/error"
const val ERROR_MESSAGE = "Message for developers"

@RestController
@RequestMapping(value = [API_PATH], produces = [APPLICATION_JSON_VALUE])
class TestResource {

  @GetMapping("$ERROR_PATH/no-such-key-exception")
  fun noSuchKeyException(httpServletRequest: HttpServletRequest): ResponseEntity<String> {
    throw NoSuchKeyException.builder().message(ERROR_MESSAGE).build()
  }
}
