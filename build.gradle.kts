plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.spring") version "2.3.10"
    kotlin("plugin.jpa") version "2.3.10"
    id("com.google.devtools.ksp") version "2.3.6"
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.9.7"
}

group = "com.ask"
version = "0.0.1-SNAPSHOT"
description = "claude"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

val querydslVersion = 7.1

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("io.github.openfeign.querydsl:querydsl-jpa:$querydslVersion")
    ksp("io.github.openfeign.querydsl:querydsl-ksp-codegen:$querydslVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("com.lemonappdev:konsist:0.17.3")
    runtimeOnly("com.h2database:h2")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

ktlint {
    filter {
        exclude { it.file.path.contains("build") }
    }
}

kover {
    reports {
        verify {
            rule {
                minBound(70)
            }
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
    processResources {
        filesMatching("**/application.yaml") {
            expand(project.properties)
        }
    }
    jar {
        enabled = false
    }
}
