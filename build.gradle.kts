import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.unbrokendome.gradle.plugins.testsets.dsl.testSets
import java.net.URL

plugins {
  val kotlinVersion = "1.4.32"
  `java-library`
  `maven-publish`
  signing
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.spring") version kotlinVersion
  id("com.ekino.oss.plugin.kotlin-quality") version "2.0.0"
  id("org.unbroken-dome.test-sets") version "4.0.0"
  id("org.jetbrains.dokka") version "1.4.32"
}

group = "com.ekino.oss.spring"
version = "5.0.0-SNAPSHOT"

repositories {
  mavenCentral()
  jcenter()
}

val springBootVersion = "2.4.5"
val awsSdkVersion = "2.16.59"
val jcvVersion = "1.5.0"
val assertkVersion = "0.24"

testSets {
  "securityTest"()
  "dataRestTest"()
  "txTest"()
  "awsTest"()
}

val securityTestImplementation by configurations
val dataRestTestImplementation by configurations
val txTestImplementation by configurations
val awsTestImplementation by configurations

dependencies {
  implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion")) // BOM import

  api("org.springframework.boot:spring-boot-autoconfigure")

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.apache.commons:commons-lang3")

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
    exclude(module = "junit")
    exclude(module = "junit-vintage-engine")
    exclude(module = "mockito-core")
    exclude(module = "assertj-core")
  }
}

val securityTest by tasks
val dataRestTest by tasks
val txTest by tasks
val awsTest by tasks

val build by tasks.named("build") {
  dependsOn(securityTest, dataRestTest, txTest, awsTest)
}

java {
  withSourcesJar()
}

jacoco {
  toolVersion = "0.8.7"
}

val javadocJar by tasks.registering(Jar::class) {
  dependsOn("dokkaHtml")
  archiveClassifier.set("javadoc")
  from(buildDir.resolve("dokka"))
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
    jvmArgs("-Duser.language=en", "-Dspring.test.constructor.autowire.mode=ALL")
  }

  dokkaHtml {
    dokkaSourceSets {
      configureEach {
        reportUndocumented.set(false)
        jdkVersion.set(8)
        externalDocumentationLink {
          url.set(URL("https://docs.spring.io/spring-framework/docs/5.2.x/javadoc-api/"))
          packageListUrl.set(URL(url.get(), "package-list"))
        }
        externalDocumentationLink {
          url.set(URL("https://docs.spring.io/spring-boot/docs/2.3.x/api/"))
          packageListUrl.set(URL(url.get(), "package-list"))
        }
      }
    }
  }

  artifacts {
    archives(jar)
    archives(javadocJar)
  }

  register("printVersion") {
    doLast {
      val version: String by project
      println(version)
    }
  }
}

val publicationName = "mavenJava"

publishing {
  publications {
    register<MavenPublication>(publicationName) {
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
        developers {
          developer {
            name.set("Clément Stoquart")
            email.set("clement.stoquart@ekino.com")
            organization.set("ekino")
            organizationUrl.set("https://www.ekino.com/")
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
      artifact(javadocJar.get())
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
