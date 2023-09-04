plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
}

val kotlinVersion = "1.9.0"
dependencies {
  implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion") // required by kotlin("jvm")
  implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion") // required by kotlin("plugin.spring")
  implementation("com.ekino.oss.plugin:kotlin-quality-plugin:4.1.0") // required by id("com.ekino.oss.plugin.kotlin-quality")
  implementation("org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVersion") // required by id("org.jetbrains.dokka")
}
