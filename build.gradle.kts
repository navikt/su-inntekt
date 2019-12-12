import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val junitJupiterVersion = "5.6.0-M1"

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "12"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "6.0.1"
}

tasks.named<Jar>("jar") {
   baseName = "app"

   manifest {
      attributes["Main-Class"] = "no.nav.su.inntekt.MainKt"
      attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
         it.name
      }
   }

   doLast {
      configurations.runtimeClasspath.get().forEach {
         val file = File("$buildDir/libs/${it.name}")
         if (!file.exists())
            it.copyTo(file)
      }
   }
}
