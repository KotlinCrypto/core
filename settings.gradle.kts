rootProject.name = "core"

@Suppress("PrivatePropertyName")
private val CHECK_PUBLICATION: String? by settings

if (CHECK_PUBLICATION != null) {
    include(":tools:check-publication")
} else {
    listOf(
        "common",
        "digest",
        "mac",
    ).forEach { name ->
        include(":library:$name")
    }
}
