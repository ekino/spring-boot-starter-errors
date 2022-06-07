plugins {
  `maven-publish`
  signing
}

group = "com.ekino.oss.spring"

val publicationName = "mavenJava"

val javadocJar by tasks.registering(Jar::class) {
  dependsOn("dokkaJavadoc")
  archiveClassifier.set("javadoc")
  from(buildDir.resolve("dokka"))
}

publishing {
  publications {
    register<MavenPublication>(publicationName) {
      artifact(javadocJar)
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
  setRequired { gradle.taskGraph.hasTask("publish") }
  sign(publishing.publications[publicationName])
}
