import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.unbrokendome.gradle.plugins.testsets.dsl.testSets

plugins {
  val kotlinVersion = "1.3.41"
  `java-library`
  `maven-publish`
  signing
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.spring") version kotlinVersion
  id("org.jlleitschuh.gradle.ktlint") version "8.1.0"
  id("org.unbroken-dome.test-sets") version "2.1.1"
}

group = "com.ekino.oss.spring"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val springBootVersion = "2.1.7.RELEASE"
val guavaVersion = "28.0-jre"

testSets {
  "securityTest"()
  "dataRestTest"()
  "txTest"()
}

val securityTestImplementation by configurations
val dataRestTestImplementation by configurations
val txTestImplementation by configurations

dependencies {
  implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion")) // BOM import

  api("org.springframework.boot:spring-boot-autoconfigure")

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.apache.commons:commons-lang3")
  implementation("com.google.guava:guava:$guavaVersion")

  compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
  compileOnly("org.springframework.boot:spring-boot-starter-data-rest")
  compileOnly("org.springframework.security:spring-security-core")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

  securityTestImplementation("org.springframework.security:spring-security-core")

  dataRestTestImplementation("org.springframework.boot:spring-boot-starter-data-rest")

  txTestImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
}

configurations {
  all {
    exclude(module = "junit")
    exclude(module = "mockito-core")
  }
}

val securityTest by tasks
val dataRestTest by tasks
val txTest by tasks

val build by tasks.named("build") {
  dependsOn(securityTest, dataRestTest, txTest)
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
      jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
  }

  withType<Test> {
    useJUnitPlatform()
    jvmArgs("-Duser.language=en")
  }
}

val publicationName = "mavenJava"

publishing {
  publications {
    create<MavenPublication>(publicationName) {
      pom {
        name.set("ekino-spring-boot-starter-errors")
        description.set("ekino-spring-boot-starter-errors configure your Spring Boot app in order to have well formatted error response in json.")
        url.set("https://github.com/ekino/spring-boot-starter-errors")
        licenses {
          license {
            name.set("MIT License (MIT)")
            url.set("https://opensource.org/licenses/mit-license")
          }
        }
        scm {
          connection.set("scm:git:git://github.com/ekino/spring-boot-starter-errors.git")
          developerConnection.set("scm:git:ssh://github.com:ekino/spring-boot-starter-errors.git")
          url.set("https://github.com/ekino/spring-boot-starter-errors")
        }
        organization {
          name.set("ekino")
          url.set("https://www.ekino.com/")
        }
      }

      from(components["java"])
    }
  }

  repositories {
    maven {
      val ossrhUrl: String? by project
      val ossrhUsername: String? by project
      val ossrhPassword: String? by project

      url = uri(ossrhUrl ?: "")

      credentials {
        username = ossrhUsername
        password = ossrhPassword
      }
    }
  }
}

signing {
  sign(publishing.publications[publicationName])
}

tasks.create("printVersion") {
  doLast {
    val version: String by project
    println(version)
  }
}
