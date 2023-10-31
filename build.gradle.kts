import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.kyleseven"
version = "1.5.4-ALPHA"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.codemc.org/repository/maven-public")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("org.apache.commons:commons-lang3:3.13.0")
    compileOnly("org.apache.logging.log4j:log4j-api:2.19.0")
    compileOnly("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
}

kotlin {
    jvmToolchain(8)
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions.javaParameters = true
    }

    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        relocate("kotlin", "me.kyleseven.consolereader.kotlin")
        relocate("co.aikar.commands", "me.kyleseven.consolereader.acf")
        relocate("co.aikar.locales", "me.kyleseven.consolereader.locales")
        relocate("org.bstats", "me.kyleseven.consolereader.bstats")
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