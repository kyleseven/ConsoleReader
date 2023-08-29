import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
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
    compileOnly("org.spigotmc:spigot-api:1.18.1-R0.1-SNAPSHOT")
    compileOnly("org.apache.logging.log4j:log4j-api:2.15.0")
    compileOnly("org.apache.logging.log4j:log4j-core:2.15.0")
    implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.0")
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions.javaParameters = true
        kotlinOptions.jvmTarget = "1.8"
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