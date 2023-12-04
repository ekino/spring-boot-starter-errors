plugins {
  id("ekino.oss.kotlin-library")
  id("ekino.oss.maven-central-publishing")
  `jvm-test-suite`
}

version = "9.0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

val springBootVersion = "3.1.5"
val awsSdkVersion = "2.21.37"

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
}

// move it back to its own file in the buildsrc folder when sharing version catalog is possible
testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {
                useJUnitJupiter()
                dependencies {
                    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
                    implementation("org.springframework.boot:spring-boot-starter-web")
                    implementation("org.springframework.boot:spring-boot-starter-validation")
                    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

                    implementation("org.springframework.boot:spring-boot-starter-test")
                    implementation("com.ekino.oss.jcv:jcv-hamcrest:1.5.0")
                    implementation("com.willowtreeapps.assertk:assertk-jvm:0.27.0")
                }
            }
        }

        val securityTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation("org.springframework.security:spring-security-web")
            }
        }

        val dataRestTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation("org.springframework.boot:spring-boot-starter-data-rest")
            }
        }

        val txTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation("org.springframework.boot:spring-boot-starter-data-jpa")
            }
        }

        val awsTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation("software.amazon.awssdk:s3:$awsSdkVersion")
            }
        }
    }
}

tasks.named("build") {
    testing.suites.forEach(::dependsOn)
}
