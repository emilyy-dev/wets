pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.fabricmc.net/")
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}

rootProject.name = "worldedit-spread-placing"

listOf("common", "bukkit", "fabric").forEach {
  include(it)
  project(":$it").name = "wets-$it"
}
