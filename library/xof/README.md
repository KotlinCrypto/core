# Module xof

`Xof` abstraction for [Extendable-Output Functions][url-pub-xof]

`XOF`s are very similar to `Digest` and `Mac` except that instead of calling `digest()`
or `doFinal()`, which returns a fixed size `ByteArray`, their output size can be variable
in length.

As such, [KotlinCrypto][url-kotlincrypto] takes the approach of making them distinctly
different from those types, while implementing the same interfaces (`Algorithm`, `Copyable`,
`Resettable`, `Updatable`).

Output for an `Xof` is done by reading, instead.

```kotlin
// Using SHAKE128 from KotlinCrypto/hash repo as an example
import org.kotlincrypto.hash.sha3.SHAKE128

fun main() {
    val xof: Xof<SHAKE128> = SHAKE128.xOf()
    val bytes = Random.Default.nextBytes(615)

    // Xof implements interface Algorithm
    println(xof.algorithm())
    // SHAKE128

    // Xof implements interface Updatable
    xof.update(5.toByte())
    xof.update(bytes)
    xof.update(bytes, 10, 88)

    // Xof implements interface Resettable
    xof.reset()

    xof.update(bytes)

    // Xof implements interface Copyable
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

```kotlin
// Using CryptoRand from KotlinCrypto/random repo as an example
import org.kotlincrypto.random.CryptoRand
// Using KMAC128 from KotlinCrypto/MACs repo as an example
import org.kotlincrypto.macs.kmac.KMAC128

fun main() {
    val key = CryptoRand.Default.nextBytes(ByteArray(100))
    val kmacXof: Xof<KMAC128> = KMAC128.xOf(key)

    // If Xof is for a Mac that implements ReKeyableXofAlgorithm,
    // reinitialize the instance via the `Xof.Companion.reset`
    // extension function for reuse.
    val newKey = CryptoRand.Default.nextBytes(ByteArray(100))
    kmacXof.reset(newKey = newKey)

    // Or zero out key material before dereferencing
    kmacXof.reset(newKey = ByteArray(1))
}
```

[url-kotlincrypto]: https://github.com/KotlinCrypto
[url-pub-xof]: https://nvlpubs.nist.gov/nistpubs/FIPS/NIST.FIPS.202.pdf
