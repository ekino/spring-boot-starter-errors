# Spring Boot starter errors

[![Build Status](https://travis-ci.org/ekino/spring-boot-starter-errors.svg?branch=master)](https://travis-ci.org/ekino/spring-boot-starter-errors)
[![GitHub (pre-)release](https://img.shields.io/github/release/ekino/spring-boot-starter-errors/all.svg)](https://github.com/ekino/spring-boot-starter-errors/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.ekino.oss.spring/ekino-spring-boot-starter-errors)](https://search.maven.org/search?q=a:ekino-spring-boot-starter-errors)

Opiniated Spring Boot starter that handles some Spring and AWS common exceptions and returns them in a standardize json format.

## Requirements

JDK 8+

Spring Boot 2.1 **MVC** web application

## Usage

For example with Gradle Kotlin DSL :

```kotlin
implementation("com.ekino.oss.spring:ekino-spring-boot-starter-errors:1.0.0")
```

NB : if you want to use snapshots you need to add the following configuration to your Gradle build script :

```kotlin
repositories {
  maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
}
```

## Configuration

Default configuration is production-ready.

Anyway, if you want your json to contain the full stacktrace you have to add the following configuration :

```yaml
ekino:
  errors:
    display-full-stacktrace: true # false by default
```

We recommend you to enable stacktraces only for local or any other development environment.

## Examples

### Bean validation error

```json
{
  "status": 400,
  "code": "error.invalid",
  "message": "Bad Request",
  "description": "Validation failed",
  "errors": [
    {
      "code": "error.missing.company",
      "field": "company",
      "message": "must not be null"
    }
  ],
  "globalErrors": [],
  "service": "my-api : POST /api/customers",
  "stacktrace": ""
}
```

### Spring security disabled account

```json
{
  "status": 403,
  "code": "error.disabled_account",
  "message": "Forbidden",
  "description": "User is disabled",
  "errors": [],
  "globalErrors": [],
  "service": "my-api : POST /api/auth",
  "stacktrace":""
}
```
