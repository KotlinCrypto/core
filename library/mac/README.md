# Module mac

`Mac` abstraction for creating & verifying Message Authentication Codes. 

Implementations can be found at [KotlinCrypto/MACs][url-macs]

```kotlin
// Using CryptoRand from KotlinCrypto/random repo as an example
import org.kotlincrypto.random.CryptoRand
// Using HmacSHA3_256 from KotlinCrypto/MACs repo as an example
import org.kotlincrypto.macs.hmac.sha3.HmacSHA3_256

fun main() {
    val key = CryptoRand.Default.nextBytes(ByteArray(100))
    val mac = HmacSHA3_256(key)
    val bytes = Random.Default.nextBytes(615)

    // Mac implements interface Algorithm
    println(mac.algorithm())
    // HmacSHA3-256

    // Mac implements interface Updatable
    mac.update(5.toByte())
    mac.update(bytes)
    mac.update(bytes, 10, 88)

    // Mac implements interface Resettable
    mac.reset()

    mac.update(bytes)

    // Mac implements interface Copyable
    val copy = mac.copy()

    val hash = mac.doFinal()
    val hash2 = copy.doFinal(bytes)
    val hash3 = ByteArray(mac.macLength())
    mac.update(bytes)
    mac.doFinalInto(hash3, destOffset = 0)

    // Reinitialize Mac instance with a new key
    // to use for something else.
    val newKey = CryptoRand.Default.nextBytes(ByteArray(100))
    mac.reset(newKey = newKey)

    // Zero out key material before dereferencing
    copy.clearKey()
}
```

[url-macs]: https://github.com/KotlinCrypto/MACs
