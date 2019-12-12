# Spring Boot starter errors

[![GitHub (pre-)release](https://img.shields.io/github/release/ekino/spring-boot-starter-errors/all.svg)](https://github.com/ekino/spring-boot-starter-errors/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.ekino.oss.spring/ekino-spring-boot-starter-errors)](https://search.maven.org/search?q=a:ekino-spring-boot-starter-errors)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ekino_spring-boot-starter-errors&metric=alert_status)](https://sonarcloud.io/dashboard?id=ekino_spring-boot-starter-errors)

Opiniated Spring Boot starter that handles some Spring and AWS common exceptions and returns them in a standardize json format.

## Requirements

JDK 8+

Spring Boot **MVC** web application

### Spring Boot compatibility

| Spring Boot | starter errors |
|:-----------:|:--------------:|
| 2.1.x       | 1.x            |
| 2.2.x       | 2.x            |

Other combinations might work but there're not supported.

## Usage

For example with Gradle Kotlin DSL :

```kotlin
implementation("com.ekino.oss.spring:ekino-spring-boot-starter-errors:1.2.0")
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
  "stacktrace": "",
  "timestamp": "2019-12-12T16:11:27.262Z"
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
  "stacktrace": "",
  "timestamp": "2019-12-12T16:11:27.262Z"
}
```
