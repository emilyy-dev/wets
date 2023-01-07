plugins {
  id("com.github.johnrengelman.shadow")
}

repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  shadow("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
  shadow("com.sk89q.worldedit:worldedit-bukkit:7.2.13")
  implementation(project(":wets-common"))
}

tasks {
  shadowJar {
    dependencies { include(project(":wets-common")) }
    archiveBaseName.set("WETS")
    archiveClassifier.set("bukkit")
  }

  build { dependsOn(shadowJar) }
}
