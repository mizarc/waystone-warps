plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.mizarc"
version = "0.3.3"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/mizarc/IF")
        credentials {
            username = project.findProperty("gpr.user") as? String ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as? String ?: System.getenv("GITHUB_TOKEN")
        }
    }
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
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    shadow("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.insert-koin:koin-core:4.0.2")
    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("dev.mizarc.inventoryframework:if:0.10.19-fork-1")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    implementation("me.xdrop:fuzzywuzzy:1.3.1")
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