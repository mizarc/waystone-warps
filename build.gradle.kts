import java.util.Properties

plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.1"
    kotlin("plugin.serialization") version "2.3.0"
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { inputStream ->
        localProperties.load(inputStream)
    }
}

fun getProperty(name: String): String {
    val localValue = localProperties.getProperty(name)
    if (localValue != null) {
        return localValue
    }
    return providers.gradleProperty(name).getOrElse("")
}

group = "dev.mizarc"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/central")
    }
    maven {
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    compileOnly("io.insert-koin:koin-core-jvm:4.1.1")
    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    compileOnly("com.zaxxer:HikariCP:7.0.2")
    // implementation("com.github.mizarc:IF:0.11.4-d")
    implementation("com.github.stefvanschie.inventoryframework:IF:0.11.6")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.xdrop:fuzzywuzzy:1.3.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier = null
}

tasks.register<Copy>("deploy") {
    dependsOn(tasks.shadowJar)
    from(layout.buildDirectory.dir("libs"))
    println("Target deployment path: ${getProperty("plugin.server.path")}")
    into(getProperty("plugin.server.path"))
    rename { fileName -> "${rootProject.name}-${version}.jar" }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.build {
    dependsOn(tasks.shadowJar)
}