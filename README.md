# Spring Boot starter errors

[![Build Status](https://travis-ci.org/ekino/spring-boot-starter-errors.svg?branch=master)](https://travis-ci.org/ekino/spring-boot-starter-errors)

Opiniated Spring Boot starter that handles Spring common exceptions and returns them in a standardize json format.

## Requirements

Only tested with Spring Boot 2.1 **MVC** web applications.

## Usage

For example with Gradle :

```kotlin
implementation("com.ekino.oss.starter:ekino-spring-boot-starter-errors:1.0.0")
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
