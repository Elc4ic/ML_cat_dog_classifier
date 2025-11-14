import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    application
    kotlin("jvm") version "2.2.0"
    id("org.graalvm.buildtools.native") version "0.10.3"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kandy-lets-plot:0.7.1")
    implementation("ar.com.hjg:pngj:2.0.1")
    testImplementation(kotlin("test"))
}

application{
    mainClass.set("org.example.MainKt")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

graalvmNative {
    binaries {
        named("main") {
            buildArgs.add("--no-fallback")
            buildArgs.add("--enable-url-protocols=file")
            buildArgs.add("--initialize-at-run-time")
            buildArgs.add("-H:+AllowIncompleteClasspath")
        }
    }
}
