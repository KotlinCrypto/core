# CHANGELOG

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
