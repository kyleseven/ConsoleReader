import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.0"
}

group = "me.kyleseven"
version = "1.5.6"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.codemc.org/repository/maven-public")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.121-stable")
    compileOnly("org.apache.logging.log4j:log4j-api:2.26.0")
    compileOnly("org.apache.logging.log4j:log4j-core:2.26.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(25)
}

tasks {
    withType<KotlinCompile>().configureEach {
        compilerOptions.javaParameters = true
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
