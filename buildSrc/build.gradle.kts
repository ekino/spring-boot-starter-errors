plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
}

val kotlinVersion = "1.5.31"
dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion") // required by kotlin("jvm")
  implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion") // required by kotlin("plugin.spring")
  implementation("gradle.plugin.com.ekino.oss.plugin:kotlin-quality-plugin:3.0.0") // required by id("com.ekino.oss.plugin.kotlin-quality")
  implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.5.30") // required by id("org.jetbrains.dokka")
  implementation("org.unbroken-dome.gradle-plugins:gradle-testsets-plugin:4.0.0") // required by id("org.unbroken-dome.test-sets")
}