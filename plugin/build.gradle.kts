@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  `java-gradle-plugin`
  kotlin("jvm")
  alias(libs.plugins.mavenPublish)
}

gradlePlugin {
  plugins {
    create("gingham") {
      id = "app.cash.gingham"
      implementationClass = "app.cash.gingham.plugin.GinghamPlugin"
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  compileOnly(libs.agp)

  implementation(libs.icu4j)
  implementation(libs.kotlinPoet)

  testImplementation(libs.junit)
  testImplementation(libs.truth)
}
