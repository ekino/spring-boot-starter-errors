import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
  `java-library`
  kotlin("jvm")
  kotlin("plugin.spring")
  id("com.ekino.oss.plugin.kotlin-quality")
  id("org.jetbrains.dokka")
}

val bytecodeVersion = 11

java {
  withSourcesJar()
}

jacoco {
  toolVersion = "0.8.7"
}

kotlin {
  jvmToolchain {
    (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(bytecodeVersion))
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

  withType<DokkaTask> {
    dokkaSourceSets {
      configureEach {
        reportUndocumented.set(false)
        jdkVersion.set(bytecodeVersion)
        externalDocumentationLink {
          url.set(URL("https://docs.spring.io/spring-framework/docs/5.3.x/javadoc-api/"))
          packageListUrl.set(URL(url.get(), "package-list"))
        }
        externalDocumentationLink {
          url.set(URL("https://docs.spring.io/spring-boot/docs/2.6.x/api/"))
          packageListUrl.set(URL(url.get(), "package-list"))
        }
      }
    }
  }

  register("printVersion") {
    doLast {
      val version: String by project
      println(version)
    }
  }
}
