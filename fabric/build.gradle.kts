plugins {
  id("fabric-loom")
}

repositories {
  maven("https://oss.sonatype.org/content/repositories/snapshots/").mavenContent { snapshotsOnly() }
  maven("https://maven.enginehub.org/repo/")
}

dependencies {
  minecraft("com.mojang:minecraft:1.19.3")
  mappings(loom.officialMojangMappings())
  modImplementation("net.fabricmc:fabric-loader:0.14.11")
  setOf(
    "fabric-command-api-v2",
    "fabric-lifecycle-events-v1"
  )
    .forEach { modImplementation(fabricApi.module(it, "0.72.0+1.19.3")) }

  modImplementation("com.sk89q.worldedit:worldedit-fabric-mc1.19.3:7.2.13")
  modImplementation("me.lucko:fabric-permissions-api:0.2-SNAPSHOT") { include(this) }
  implementation(project(":wets-common")) { include(this) }
}

tasks {
  remapJar {
    archiveBaseName.set("WETS")
    archiveClassifier.set("fabric")
  }

  assemble {
    dependsOn(remapJar)
  }

  runClient {
    javaLauncher.set(project.javaToolchains.launcherFor(project.java.toolchain))
  }
}
