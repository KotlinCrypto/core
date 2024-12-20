# core
[![badge-license]][url-license]
[![badge-latest-release]][url-latest-release]

[![badge-kotlin]][url-kotlin]
[![badge-endians]][url-endians]

![badge-platform-android]
![badge-platform-jvm]
![badge-platform-js]
![badge-platform-js-node]
![badge-platform-wasm]
![badge-platform-linux]
![badge-platform-macos]
![badge-platform-ios]
![badge-platform-tvos]
![badge-platform-watchos]
![badge-platform-windows]
![badge-support-android-native]
![badge-support-apple-silicon]
![badge-support-js-ir]
![badge-support-linux-arm]

Low level core cryptographic components for Kotlin Multiplatform

NOTE: For Jvm, `Digest` extends `java.security.MessageDigest` and `Mac` extends `javax.crypto.Mac` 
for interoperability.

Utilized by [KotlinCrypto/hash][url-hash] and [KotlinCrypto/MACs][url-macs]

### Library Authors

Modules in `core` are intentionally **single purpose** and **small** such that you 
are able to include them in your APIs without having to import some massive crypto 
library. Consumers of your APIs can then import the higher level implementations 
to use with your library (giving them the **choice** of what algorithms they wish 
to use, importing only what they need).

This also means that as new higher level functions get implemented, you do not need 
to do anything.

```kotlin
/**
 * This feature of Foo requires a dependency if you wish to use it.
 * See: https://github.com/KotlinCrypto/hash
 * */
class FooFeatureA(digest: Digest) {
    // ...
}
```

### Usage

<details>
    <summary>Digest</summary>

```kotlin
// Using SHA256 from hash repo as an example
import org.kotlincrypto.hash.sha2.SHA256

fun main() {
    val digest = SHA256()
    val bytes = Random.Default.nextBytes(615)
    
    // Digest implements Algorithm
    println(digest.algorithm())
    
    // Digest implements Updatable
    digest.update(5.toByte())
    digest.update(bytes)
    digest.update(bytes, 10, 88)

    // Digest implements Resettable
    digest.reset()

    digest.update(bytes)

    // Digest implements Copyable
    val copy = digest.copy()

    val hash = digest.digest()
    val hash2 = copy.digest(bytes)
}
```

</details>

<details>
    <summary>Mac</summary>

```kotlin
// Using SecureRandom from the secure-random repo as an example
import org.kotlincrypto.SecureRandom
// Using HmacSHA3_256 from the MACs repo as an example
import org.kotlincrypto.macs.hmac.sha3.HmacSHA3_256

fun main() {
    val key = SecureRandom().nextBytesOf(100)
    val mac = HmacSHA3_256(key)
    val bytes = Random.Default.nextBytes(615)

    // Mac implements Algorithm
    println(mac.algorithm())

    // Mac implements Updatable
    mac.update(5.toByte())
    mac.update(bytes)
    mac.update(bytes, 10, 88)

    // Mac implements Resettable
    mac.reset()

    mac.update(bytes)

    // Mac implements Copyable
    val copy = mac.copy()

    val hash = mac.doFinal()
    val hash2 = copy.doFinal(bytes)
}
```

</details>

<details>
    <summary>Xof</summary>

`XOF`s (i.e. [Extendable-Output Functions][url-pub-xof]) were introduced with `SHA3`.

`XOF`s are very similar to `Digest` and `Mac` except that instead of calling `digest()` 
or `doFinal()`, which returns a fixed size `ByteArray`, their output size can be variable 
in length.

As such, [KotlinCrypto][url-kotlin-crypto] takes the approach of making them distinctly 
different from those types, while implementing the same interfaces (`Algorithm`, `Copyable`, 
`Resettable`, `Updatable`).

Output for an `Xof` is done by reading, instead.

```kotlin
// Using SHAKE128 from hash repo as an example
import org.kotlincrypto.hash.sha3.SHAKE128

fun main() {
    val xof: Xof<SHAKE128> = SHAKE128.xOf()
    val bytes = Random.Default.nextBytes(615)

    // Xof implements Algorithm
    println(xof.algorithm())

    // Xof implements Updatable
    xof.update(5.toByte())
    xof.update(bytes)
    xof.update(bytes, 10, 88)

    // Xof implements Resettable
    xof.reset()

    xof.update(bytes)

    // Xof implements Copyable
    xof.copy()

    val out1 = ByteArray(100)
    val out2 = ByteArray(12345)

    // Use produces a Reader which auto-closes when your action finishes.
    // Reader is using a snapshot of the Xof state (thus the
    // optional argument to resetXof with a default of true).
    xof.use(resetXof = false) { read(out1, 0, out1.size); read(out2) }

    val out3 = ByteArray(out1.size)
    val out4 = ByteArray(out2.size)

    // Can also create a Reader that won't auto-close
    val reader = xof.reader(resetXof = false)
    reader.read(out3)
    reader.read(out4)
    reader.close()

    try {
        // The Reader has been closed and will throw
        // exception when trying to read from again.
        reader.use { read(out4) }
    } catch (e: IllegalStateException) {
        e.printStackTrace()
    }

    // Contents are the same because Reader uses
    // a snapshot of Xof, which was not updated
    // between production of Readers.
    assertContentEquals(out1 + out2, out3 + out4)

    // Still able to update Xof, independent of the production
    // and usage of Readers.
    xof.update(10.toByte())
    xof.use { read(out3); read(out4) }

    try {
        assertContentEquals(out1 + out2, out3 + out4)
        throw IllegalStateException()
    } catch (_: AssertionError) {
        // pass
    }
}
```

</details>

### Get Started

The best way to keep `KotlinCrypto` dependencies up to date is by using the 
[version-catalog][url-version-catalog]. Alternatively, see below.

<!-- TAG_VERSION -->

```kotlin
// build.gradle.kts
dependencies {
    val core = "0.5.5"
    implementation("org.kotlincrypto.core:digest:$core")
    implementation("org.kotlincrypto.core:mac:$core")
    implementation("org.kotlincrypto.core:xof:$core")
}
```

<!-- TAG_VERSION -->

```groovy
// build.gradle
dependencies {
    def core = "0.5.5"
    implementation "org.kotlincrypto.core:digest:$core"
    implementation "org.kotlincrypto.core:mac:$core"
    implementation "org.kotlincrypto.core:xof:$core"
}
```

<!-- TAG_VERSION -->
[badge-latest-release]: https://img.shields.io/badge/latest--release-0.5.5-blue.svg?style=flat
[badge-license]: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat

<!-- TAG_DEPENDENCIES -->
[badge-kotlin]: https://img.shields.io/badge/kotlin-1.9.24-blue.svg?logo=kotlin
[badge-endians]: https://img.shields.io/badge/kotlincrypto.endians-0.3.1-blue.svg

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
[url-kotlin-crypto]: https://github.com/KotlinCrypto
[url-endians]: https://github.com/KotlinCrypto/endians
[url-hash]: https://github.com/KotlinCrypto/hash
[url-macs]: https://github.com/KotlinCrypto/MACs
[url-version-catalog]: https://github.com/KotlinCrypto/version-catalog
[url-pub-xof]: https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.202.pdf
