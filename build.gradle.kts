import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "me.kyleseven"
version = "1.5.1-ALPHA"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.2-R0.1-SNAPSHOT")
    compileOnly("org.apache.logging.log4j:log4j-api:2.13.2")
    compileOnly("org.apache.logging.log4j:log4j-core:2.13.2")
    implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.javaParameters = true
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        relocate("kotlin", "me.kyleseven.consolereader.kotlin")
        relocate("co.aikar.commands", "me.kyleseven.consolereader.acf")
        relocate("co.aikar.locales", "me.kyleseven.locales")
        minimize()
    }

    named<ProcessResources>("processResources") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            filter<ReplaceTokens>("tokens" to mapOf("version" to version))
        }
    }

    build {
        dependsOn("shadowJar")
    }
}