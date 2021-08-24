import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate

plugins {
  id("org.unbroken-dome.test-sets")
}

testSets {
  "securityTest"()
  "dataRestTest"()
  "txTest"()
  "awsTest"()
}

val securityTest by tasks
val dataRestTest by tasks
val txTest by tasks
val awsTest by tasks

val build by tasks.named("build") {
  dependsOn(securityTest, dataRestTest, txTest, awsTest)
}
