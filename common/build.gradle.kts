plugins {
  `java-library`
}

repositories {
  maven("https://maven.enginehub.org/repo/")
}

dependencies {
  api("com.sk89q.worldedit:worldedit-core:7.2.14")
  api("it.unimi.dsi:fastutil:8.5.9")
  compileOnlyApi("org.jetbrains:annotations:23.0.0")
}
