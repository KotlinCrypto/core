# Module digest

`Digest` abstraction for hashing. 

Implementations can be found at [KotlinCrypto/hash][url-hash]

```kotlin
// Using SHA256 from hash repo as an example
import org.kotlincrypto.hash.sha2.SHA256

fun main() {
    val digest = SHA256()
    val bytes = Random.Default.nextBytes(615)
    
    // Digest implements interface Algorithm
    println(digest.algorithm())
    // SHA-256
    
    // Digest implements interface Updatable
    digest.update(5.toByte())
    digest.update(bytes)
    digest.update(bytes, 10, 88)

    // Digest implements interface Resettable
    digest.reset()

    digest.update(bytes)

    // Digest implements interface Copyable
    val copy = digest.copy()

    val hash = digest.digest()
    val hash2 = copy.digest(bytes)
    val hash3 = ByteArray(digest.digestLength())
    digest.update(bytes)
    digest.digestInto(hash3, destOffset = 0)
}
```

[url-hash]: https://github.com/KotlinCrypto/hash
