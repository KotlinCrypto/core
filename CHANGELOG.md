# CHANGELOG

## Version 0.2.7 (2023-06-09)
 - Fixes Android API 23 and below not calling `javax.crypto.MacSpi.engineReset`
   whenever `javax.crypto.Mac.doFinal` is invoked [[#46]][46]

## Version 0.2.6 (2023-06-08)
 - Fixes Android API 21-23 requiring a `Provider` in order to set
   `javax.crypto.Mac.spiImpl` when `javax.crypto.Mac.init` is
   invoked [[#44]][44]
 - Throw `InvalidKeyException` if `javax.crypto.Mac.init` is invoked [[#43]][43]
     - All `org.kotlincrypto.core.Mac` APIs are constructed such that
       implementations always require a key as a constructor argument
       and are initialized immediately. As such, if a java caller
       attempts to re-initialize the `Mac` with a different key, they
       may assume the output thus produced is using the new key. This
       is not the case as `Kotlin Crypto` does not use a provider based
       architecture so a new, uninitialized `Mac` cannot be created.
     - Note that `Mac.init` is **not** available from `commonMain`. It is
       a remnant of bad API design requiring ability to lazily initialize
       things which `Kotlin Crypto` will **never** support as it leads
       to monolithic structures, instead of building on good abstractions.
       If `Mac.init` is required to be called, a wholly new instance of the
       `org.kotlincrypto.core.Mac` implementation should be instantiated
       with the new key.

## Version 0.2.5 (2023-06-07)
 - Fixes Android API 23 and below not accepting `null` for `Mac.init` key
   parameter [[#38]][38]
 - Updates `kotlin` to `1.8.21` [[#40]][40]

## Version 0.2.4 (2023-04-16)
 - Adds `Digest.updateDigest` protected open functions for implementors 
   to override when needing access to input before it is buffered [[#34]][34]
 - Adds input argument check for nonJvm `Mac.update` when `offset` and `len` 
   parameters are specified [[#35]][35]
 - Updates `Digest.digestLength` constructor argument check to now accept 0 
   as a valid length [[#36]][36]
     - Previously, passing 0 would throw an `IllegalArgumentException`.

## Version 0.2.3 (2023-04-08)
 - Fix `nonJvm` `Mac.doFinal` not calling `engine.reset()` [[#27]][27]
     - Only implementation of `Mac` is `Hmac` via `MACs` repo, which is
       unaffected by this issue.
 - Adds `XofAlgorithm` interface [[#29]][29]

## Version 0.2.2 (2023-04-07)
 - Adds abstraction for `XOF`s (`Extendable-Output Functions`) [[#25]][25]

## Version 0.2.0 (2023-03-28)
 - **BREAKING CHANGE:**
     - `Digest.compress` function was changed to also include an offset.
       This drastically improves performance by mitigating excessive/unnecessary
       array copying. [[#21]][21]

## Version 0.1.1 (2023-03-06)
 - Fix `Digest.update` miscalculation when `offset` parameter is provided [[#16]][16]
 - Fix `jvm` `Digest.algorithm()` StackOverflow error [[#12]][12]

## Version 0.1.0 (2023-03-04)
 - Initial Release

[12]: https://github.com/KotlinCrypto/core/pull/12
[16]: https://github.com/KotlinCrypto/core/pull/16
[21]: https://github.com/KotlinCrypto/core/pull/21
[25]: https://github.com/KotlinCrypto/core/pull/25
[27]: https://github.com/KotlinCrypto/core/pull/27
[29]: https://github.com/KotlinCrypto/core/pull/29
[34]: https://github.com/KotlinCrypto/core/pull/34
[35]: https://github.com/KotlinCrypto/core/pull/35
[36]: https://github.com/KotlinCrypto/core/pull/36
[38]: https://github.com/KotlinCrypto/core/pull/38
[40]: https://github.com/KotlinCrypto/core/pull/40
[43]: https://github.com/KotlinCrypto/core/pull/43
[44]: https://github.com/KotlinCrypto/core/pull/44
[46]: https://github.com/KotlinCrypto/core/pull/46
