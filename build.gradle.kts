plugins {
  id("com.github.johnrengelman.shadow") version "7.1.2" apply false
  id("fabric-loom") version "1.3-SNAPSHOT" apply false
}

subprojects {
  apply(plugin = "java")

  extensions.configure<JavaPluginExtension>("java") {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
  }

  repositories {
    mavenCentral().mavenContent { releasesOnly() }
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
