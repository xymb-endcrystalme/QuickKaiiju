import io.papermc.paperweight.util.*
import io.papermc.paperweight.util.constants.*

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.patcher") version "1.5.4"
}

allprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

repositories {
    mavenCentral()
    maven(paperMavenPublicUrl) {
        content {
            onlyForConfigurations(PAPERCLIP_CONFIG)
        }
    }
}

dependencies {
    remapper("net.fabricmc:tiny-remapper:0.8.6:fat")
    decompiler("net.minecraftforge:forgeflower:2.0.629.0")
    paperclip("io.papermc:paperclip:3.0.3")
}

subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
        maven("https://jitpack.io")
    }
}

paperweight {
    serverProject.set(project(":QuickKaiiju-Server"))

    remapRepo.set(paperMavenPublicUrl)
    decompileRepo.set(paperMavenPublicUrl)

    useStandardUpstream("Kaiiju") {
        url.set(github("Endcrystal-me", "Kaiiju"))
        ref.set(providers.gradleProperty("kaiijuRef"))
        
        withStandardPatcher {
            baseName("Kaiiju")

            apiPatchDir.set(layout.projectDirectory.dir("patches/api"))
            apiOutputDir.set(layout.projectDirectory.dir("QuickKaiiju-API"))

            serverPatchDir.set(layout.projectDirectory.dir("patches/server"))
            serverOutputDir.set(layout.projectDirectory.dir("QuickKaiiju-Server"))
        }
    }
}

tasks.register("kaiijuRefLatest") {
    // Update the kaiijuRef in gradle.properties to be the latest commit.
    val tempDir = layout.cacheDir("kaiijuRefLatest");
    val file = "gradle.properties";
        
    doFirst {
        data class GithubCommit(
                val sha: String
        )

        val kaiijuLatestCommitJson = layout.cache.resolve("kaiijuLatestCommit.json");
        download.get().download("https://api.github.com/repos/KaiijuMC/Kaiiju/commits/ver/1.20.1", kaiijuLatestCommitJson);
        val kaiijuLatestCommit = gson.fromJson<paper.libs.com.google.gson.JsonObject>(kaiijuLatestCommitJson)["sha"].asString;

        copy {
            from(file)
            into(tempDir)
            filter { line: String ->
                line.replace("kaiijuRef = .*".toRegex(), "kaiijuRef = $kaiijuLatestCommit")
            }
        }
    }

    doLast {
        copy {
            from(tempDir.file("gradle.properties"))
            into(project.file(file).parent)
        }
    }
}
