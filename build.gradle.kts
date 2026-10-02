import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm") version "2.2.20"
  kotlin("plugin.spring") version "2.2.20"
  kotlin("plugin.jpa") version "2.2.20"
  id("org.springframework.boot") version "4.0.0"
  jacoco
}

group = "com.simpsonm09"
version = "0.1.0"

// The build runs on the Temurin 25 JDK pinned in mise.toml. Kotlin 2.2 tops out at
// JVM target 24, so javac is pinned to the same release to keep the targets consistent.
java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 24
}

repositories {
  mavenCentral()
}

// springdoc 3.1.1 pulls spring-boot-webmvc 4.1.0 transitively, which wins Gradle's conflict
// resolution and breaks the Boot 4.0 actuator handler. Hold every Boot module on the platform line.
configurations.configureEach {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.springframework.boot") {
      useVersion("4.0.0")
      because("keep every Boot module on the imported platform version")
    }
  }
}

dependencies {
  implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.0"))
  // Boot 4 deprecates this starter in favour of spring-boot-starter-webmvc.
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.1")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  runtimeOnly("com.h2database:h2")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  // MockMvc moved out of spring-boot-starter-test in the Boot 4 modular split.
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  testImplementation("io.rest-assured:rest-assured:5.5.0")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.add("-Xjsr305=strict")
    jvmTarget.set(JvmTarget.JVM_24)
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

jacoco {
  toolVersion = "0.8.13"
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required.set(true)
    html.required.set(true)
    csv.required.set(false)
  }
}

// Regenerates docs/openapi.json from the running application context. `just spec`
// calls this, and the test job fails when regeneration changes the checked-in file.
val generateOpenApi by tasks.registering(Test::class) {
  description = "Regenerate docs/openapi.json from the application"
  group = "documentation"
  testClassesDirs = sourceSets["test"].output.classesDirs
  classpath = sourceSets["test"].runtimeClasspath
  useJUnitPlatform()
  filter {
    includeTestsMatching("com.simpsonm09.template.OpenApiDocumentTest")
  }
  shouldRunAfter(tasks.test)
}
