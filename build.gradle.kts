plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.mizarc"
version = "0.1-SNAPSHOT"

repositories {
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
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    shadow("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.github.stefvanschie.inventoryframework:IF:0.10.11")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
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