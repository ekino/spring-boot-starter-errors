import org.gradle.api.JavaVersion
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
  `java-library`
  kotlin("jvm")
  kotlin("plugin.spring")
  id("com.ekino.oss.plugin.kotlin-quality")
  id("org.jetbrains.dokka")
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

kotlin {
  jvmToolchain {
    (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
  }
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      freeCompilerArgs = listOf("-Xjsr305=strict")
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
          url.set(URL("https://docs.spring.io/spring-framework/docs/5.3.x/javadoc-api/"))
          packageListUrl.set(URL(url.get(), "package-list"))
        }
        externalDocumentationLink {
          url.set(URL("https://docs.spring.io/spring-boot/docs/2.5.x/api/"))
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
