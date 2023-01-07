rootProject.name = "worldedit-spread-placing"

pluginManagement {
  repositories {
    maven("https://maven.fabricmc.net/")
    mavenCentral()
    gradlePluginPortal()
  }
}

listOf("common", "bukkit", "fabric").forEach {
  include(it)
  project(":$it").name = "wets-$it"
}
