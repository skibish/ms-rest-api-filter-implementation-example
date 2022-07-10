import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
  kotlin("jvm") version "1.7.10"
  application
  antlr
}

group = "com.github.skibish"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  antlr("org.antlr:antlr4:4.10.1")

  testImplementation(kotlin("test"))
  testImplementation("org.junit.jupiter:junit-jupiter-params")
}

tasks.test {
  useJUnitPlatform()
}

tasks.generateGrammarSource {
  maxHeapSize = "64m"
  arguments = arguments + listOf("-long-messages")
}

tasks.named("compileTestKotlin") {
  dependsOn(":generateTestGrammarSource")
}

tasks.named("compileKotlin") {
  dependsOn(":generateGrammarSource")
}

tasks.named("generateGrammarSource") {
  dependsOn(":runKtlintCheckOverMainSourceSet")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "18"
}

application {
  mainClass.set("com.github.skibish.filterexample.MainKt")
}

tasks.named("run", JavaExec::class) {
  standardInput = System.`in`
}
