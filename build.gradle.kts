import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.unbrokendome.gradle.plugins.testsets.dsl.testSets
import java.net.URL

plugins {
  val kotlinVersion = "1.3.61"
  `java-library`
  `maven-publish`
  signing
  kotlin("jvm") version kotlinVersion
  kotlin("plugin.spring") version kotlinVersion
  id("org.jlleitschuh.gradle.ktlint") version "9.1.1"
  id("org.unbroken-dome.test-sets") version "2.2.1"
  id("org.jetbrains.dokka") version "0.10.0"
}

group = "com.ekino.oss.spring"
version = "2.0.0-SNAPSHOT"

repositories {
  mavenCentral()
  jcenter()
}

val springBootVersion = "2.1.10.RELEASE"
val guavaVersion = "28.1-jre"
val awsSdkVersion = "2.7.22"
val jcvVersion = "1.4.1"

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
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.apache.commons:commons-lang3")
  implementation("com.google.guava:guava:$guavaVersion")

  compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
  compileOnly("org.springframework.boot:spring-boot-starter-data-rest")
  compileOnly("org.springframework.security:spring-security-core")
  compileOnly("software.amazon.awssdk:s3:$awsSdkVersion")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testImplementation("com.ekino.oss.jcv:jcv-hamcrest:$jcvVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

  securityTestImplementation("org.springframework.security:spring-security-core")

  dataRestTestImplementation("org.springframework.boot:spring-boot-starter-data-rest")

  txTestImplementation("org.springframework.boot:spring-boot-starter-data-jpa")

  awsTestImplementation("software.amazon.awssdk:s3:$awsSdkVersion")
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
val awsTest by tasks

val build by tasks.named("build") {
  dependsOn(securityTest, dataRestTest, txTest, awsTest)
}

val sourcesJar by tasks.registering(Jar::class) {
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allJava)
}

val javadocJar by tasks.registering(Jar::class) {
  dependsOn("dokka")
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
    jvmArgs("-Duser.language=en")
  }

  withType<DokkaTask> {
    configuration {
      reportUndocumented = false
      jdkVersion = 8
      externalDocumentationLink {
        url = URL("https://docs.spring.io/spring-framework/docs/5.1.9.RELEASE/javadoc-api/")
        packageListUrl = URL(url, "package-list")
      }
      externalDocumentationLink {
        url = URL("https://docs.spring.io/spring-boot/docs/2.1.x/api/")
        packageListUrl = URL(url, "package-list")
      }
    }
  }

  withType<GenerateModuleMetadata>().configureEach {
    enabled = false
  }

  artifacts {
    archives(jar)
    archives(sourcesJar)
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

      artifact(sourcesJar.get())
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
