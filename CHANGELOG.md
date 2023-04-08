# CHANGELOG

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
