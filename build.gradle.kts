plugins {
  id("com.github.johnrengelman.shadow") version "7.1.2" apply false
  id("fabric-loom") version "1.0-SNAPSHOT" apply false
}

subprojects {
  apply(plugin = "java")

  repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
  }

  dependencies {
    "implementation"("com.sk89q.worldedit:worldedit-core:7.2.13")
    "implementation"("it.unimi.dsi:fastutil:8.5.9")
    "compileOnly"("org.jetbrains:annotations:23.0.0")
  }

  tasks {
    named<JavaCompile>("compileJava") {
      options.release.set(17)
    }

    named<ProcessResources>("processResources") {
      inputs.property("version", this@subprojects.version)
      filesNotMatching("**/icon.png") {
        expand("version" to this@subprojects.version)
      }
    }

    withType<Jar> {
      metaInf.from(rootProject.file("LICENSE")).into("ar.emily/wets")
    }
  }
}
