plugins {
    application
    kotlin("jvm") version "1.3.72"

}

version = "1.0.2"
group = "org.clintrorick"

application {
    mainClass.set("org.clintrorick.GameDriverKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.module","jackson-module-kotlin","2.9.8")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.7")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.3.7")



}
