# core
[![badge-license]][url-license]
[![badge-latest-release]][url-latest-release]

[![badge-kotlin]][url-kotlin]
<!-- TODO: [![badge-endians]][url-endians] -->

![badge-platform-android]
![badge-platform-jvm]
![badge-platform-js]
![badge-platform-js-node]
![badge-platform-linux]
![badge-platform-macos]
![badge-platform-ios]
![badge-platform-tvos]
![badge-platform-watchos]
![badge-platform-wasm]
![badge-platform-windows]
![badge-support-android-native]
![badge-support-apple-silicon]
![badge-support-js-ir]
![badge-support-linux-arm]
![badge-support-linux-mips]

Low level core cryptographic components for Kotlin Multiplatform

This library has very low level APIs that are used in higher level 
implementations. You most likely never will need to add this as a
dependency.

If you are looking for Hashing algorithms (e.g. `SHA-256`, `SHA-512`, etc), see the [hash repo][url-hash].

If you are looking for Mac algorithms (e.g. `HmacSHA256`, `HmacSHA512`, etc), see the [MACs repo][url-macs].

### Get Started

The best way to keep `KotlinCrypto` dependencies up to date is by using the 
[version-catalog][url-version-catalog]. Alternatively, see below.

<!-- TAG_VERSION -->
<!-- TODO: implementation("org.kotlincrypto.core:xof:$core") -->

```kotlin
// build.gradle.kts
dependencies {
    val core = "0.2.0"
    implementation("org.kotlincrypto.core:digest:$core")
    implementation("org.kotlincrypto.core:mac:$core")
}
```

<!-- TAG_VERSION -->
<!-- TODO: implementation "org.kotlincrypto.core:xof:$core" -->

```groovy
// build.gradle
dependencies {
    def core = "0.2.0"
    implementation "org.kotlincrypto.core:digest:$core"
    implementation "org.kotlincrypto.core:mac:$core"
}
```

<!-- TAG_VERSION -->
[badge-latest-release]: https://img.shields.io/badge/latest--release-0.2.0-blue.svg?style=flat
[badge-license]: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat

<!-- TAG_DEPENDENCIES -->
[badge-kotlin]: https://img.shields.io/badge/kotlin-1.8.10-blue.svg?logo=kotlin
[badge-endians]: https://img.shields.io/badge/kotlincrypto.endians-0.1.0-blue.svg

<!-- TAG_PLATFORMS -->
[badge-platform-android]: http://img.shields.io/badge/-android-6EDB8D.svg?style=flat
[badge-platform-jvm]: http://img.shields.io/badge/-jvm-DB413D.svg?style=flat
[badge-platform-js]: http://img.shields.io/badge/-js-F8DB5D.svg?style=flat
[badge-platform-js-node]: https://img.shields.io/badge/-nodejs-68a063.svg?style=flat
[badge-platform-linux]: http://img.shields.io/badge/-linux-2D3F6C.svg?style=flat
[badge-platform-macos]: http://img.shields.io/badge/-macos-111111.svg?style=flat
[badge-platform-ios]: http://img.shields.io/badge/-ios-CDCDCD.svg?style=flat
[badge-platform-tvos]: http://img.shields.io/badge/-tvos-808080.svg?style=flat
[badge-platform-watchos]: http://img.shields.io/badge/-watchos-C0C0C0.svg?style=flat
[badge-platform-wasm]: https://img.shields.io/badge/-wasm-624FE8.svg?style=flat
[badge-platform-windows]: http://img.shields.io/badge/-windows-4D76CD.svg?style=flat
[badge-support-android-native]: http://img.shields.io/badge/support-[AndroidNative]-6EDB8D.svg?style=flat
[badge-support-apple-silicon]: http://img.shields.io/badge/support-[AppleSilicon]-43BBFF.svg?style=flat
[badge-support-js-ir]: https://img.shields.io/badge/support-[js--IR]-AAC4E0.svg?style=flat
[badge-support-linux-arm]: http://img.shields.io/badge/support-[LinuxArm]-2D3F6C.svg?style=flat
[badge-support-linux-mips]: http://img.shields.io/badge/support-[LinuxMIPS]-2D3F6C.svg?style=flat

[url-latest-release]: https://github.com/KotlinCrypto/core/releases/latest
[url-license]: https://www.apache.org/licenses/LICENSE-2.0.txt
[url-kotlin]: https://kotlinlang.org
[url-endians]: https://github.com/KotlinCrypto/endians
[url-hash]: https://github.com/KotlinCrypto/hash
[url-macs]: https://github.com/KotlinCrypto/MACs
[url-version-catalog]: https://github.com/KotlinCrypto/version-catalog
