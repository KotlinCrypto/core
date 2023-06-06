rootProject.name = "core"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

includeBuild("build-logic")

@Suppress("PrivatePropertyName")
private val CHECK_PUBLICATION: String? by settings

if (CHECK_PUBLICATION != null) {
    include(":tools:check-publication")
} else {
    listOf(
        "common",
        "digest",
        "mac",
        "xof",
    ).forEach { name ->
        include(":library:$name")
    }

    include(":test-android")
}
