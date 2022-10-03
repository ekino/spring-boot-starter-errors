plugins {
  id("ekino.oss.kotlin-library")
  id("ekino.oss.maven-central-publishing")
  id("ekino.oss.test")
}

version = "8.0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

val springBootVersion = "2.7.4"
val awsSdkVersion = "2.17.285"
val jcvVersion = "1.5.0"
val assertkVersion = "0.25"

dependencies {
  implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion")) // BOM import

  api("org.springframework.boot:spring-boot-autoconfigure")

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  compileOnly("org.jetbrains.kotlin:kotlin-reflect")
  compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
  compileOnly("org.springframework.boot:spring-boot-starter-data-rest")
  compileOnly("org.springframework.security:spring-security-web")
  compileOnly("software.amazon.awssdk:s3:$awsSdkVersion")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("com.ekino.oss.jcv:jcv-hamcrest:$jcvVersion")
  testImplementation("com.willowtreeapps.assertk:assertk-jvm:$assertkVersion")

  securityTestImplementation("org.springframework.security:spring-security-web")

  dataRestTestImplementation("org.springframework.boot:spring-boot-starter-data-rest")

  txTestImplementation("org.springframework.boot:spring-boot-starter-data-jpa")

  awsTestImplementation("software.amazon.awssdk:s3:$awsSdkVersion")
}

configurations {
  all {
    exclude(module = "mockito-core")
    exclude(module = "assertj-core")
  }
}
