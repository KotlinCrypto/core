# CHANGELOG

## Version 0.7.0 (2025-02-25)
 - Updates `kotlin` to `2.1.10` [[#122]][122]
 - Updates `kotlincrypto.error` to `0.3.0` [[#122]][122]
 - `Mac` and `Digest` constructors now throw `InvalidParameterException` instead 
   of `IllegalArgumentException` [[#124]][124]
 - `Mac.Engine` constructor, `Mac.reset(newKey)`, and `Xof.Companion.reset(newKey)` now 
   throw `InvalidKeyException` instead of `IllegalArgumentException` [[#124]][124]

## Version 0.6.1 (2025-02-09)
 - Adds `Digest.digestInto` API for populating an existing `ByteArray` with output [[#108]][108]
 - Adds `Mac.doFinalInto` API for populating an existing `ByteArray` with output [[#109]][109]
 - Adds `Mac.Engine.resetOnDoFinal` constructor argument for providing implementations the ability 
   to disable automatic `Engine.reset` call when `doFinal` or `doFinalInto` gets executed [[#111]][111]
     - Mitigates double resets when implementation is backed by a `Digest`.
 - Adds `dokka` documentation at `https://core.kotlincrypto.org` [[#116]][116]
 - Adds API dependency `org.kotlincrypto:error` to module `core` [[#118]][118]

## Version 0.6.0 (2025-01-15)
 - `@Throws` annotation removed from `Updatable.update` (it is documented).
 - Finalizes `Digest` internal API and removes `InternalKotlinCryptoApi` opt-in requirement from constructors.
     - Drops usage of `DigestState` in favor of secondary constructor which takes 
       `Digest` as an argument (for `copy` function implementations).
     - `Copyable.copy` override is now passed through to implementations so that the proper 
       return type can be defined (instead of requiring API consumers to cast from `Digest`).
     - `protected` functions renamed:
         - `compress` >> `compressProtected`
         - `digest` >> `digestProtected`
         - `updateDigest` >> `updateProtected`
         - `resetDigest` >> `resetProtected`
     - `digestProtected` (previously `digest`) now only provides the buffered input and position as 
       arguments; `bitLength` is no longer tracked by `Digest`.
 - `Digest.digest` now zero's out stale buffered input from `bufPos` to `buf.size` before passing it to 
   `digestProtected` (previously `digest`) as an argument.
 - Finalizes `Mac` internal API and removes `InternalKotlinCryptoApi` opt-in requirement from constructors.
     - Provides secondary constructor which takes `Mac` as an argument (for `copy` function implementations).
     - `Copyable.copy` override is now passed through to `Mac` implementations so that the proper 
       return type can be defined (instead of requiring API consumers to cast from `Mac`).
     - Adds ability to reinitialize `Mac` with a new `key` parameter via `reset(newKey)` (or clear it after use).
     - Adds `Mac.clearKey` helper function for zeroing out key material before dereferencing the `Mac` (if desired).
     - Removes `Mac.Engine.State` in favor of secondary constructor which takes `Mac.Engine` as an argument 
       (for `copy` function implementation).
     - Adds abstract function `Mac.Engine.reset(newKey)` for reinitialization functionality.
 - Finalizes `Xof` and `XofFactory` internal API and removes `InternalKotlinCryptoApi` opt-in 
   requirement from constructors.
     - `Xof.Reader.readProtected` no longer passes `bytesRead` as an argument.
 - Adds `ReKeyableXofAlgorithm` interface for `Xof` implementations who's backing delegate is a `Mac`.
 - Adds `Xof.Companion.reset(newKey)` extension function which allows API consumers the ability to 
   reinitialize the `Xof` with a new `key` parameter when that `Xof` is backed by an instance of 
   `ReKeyableXofAlgorithm` (i.e. a `Mac` implementation).
 - `Xof.use` and `Xof.Reader.use` functions are now inlined.
 - Removes usage of `KotlinCrypto.endians` library (deprecated) from `Xof.Utils`.

## Version 0.5.5 (2024-12-20)
 - Fixes optimization issues with `Digest.update` internals [[#70]][70]

## Version 0.5.4 (2024-12-19)
 - Adds benchmarking to repository [[#64]][64]
 - Refactors `Digest` internals (performance improvements) [[#66]][66]
 - Changes module `:common` name to `:core` to be more in line with its 
   package name of `org.kotlincrypto.core`
     - Publication coordinates have changed from `org.kotlincrypto.core:common` 
       to `org.kotlincrypto.core:core`

## Version 0.5.3 (2024-08-31)
 - Updates `kotlin` to `1.9.24` [[#61]][61]
 - Updates `endians` to `0.3.1` [[#61]][61]
 - Fixes multiplatform metadata manifest `unique_name` parameter for 
   all source sets to be truly unique. [[#61]][61]
 - Updates jvm `.kotlin_module` with truly unique file name. [[#61]][61]

## Version 0.5.1 (2024-03-18)
 - Use `transitive` keyword for `JPMS` `module-info.java` files [[#58]][58]

## Version 0.5.0 (2024-03-18)
 - Updates `kotlin` to `1.9.23` [[#55]][55]
 - Updates `endians` to `0.3.0` [[#55]][55]
 - Add experimental support for `wasmJs` & `wasmWasi` [[#55]][55]
 - Add support for Java9 `JPMS` via Multi-Release jar [[#56]][56]

## Version 0.4.0 (2023-11-30)
 - Adds check for Android Runtime to `KC_ANDROID_SDK_INT` [[#51]][51]
     - Android Unit Tests: `KC_ANDROID_SDK_INT` will now be `null`
     - Android Runtime: `KC_ANDROID_SDK_INT` will **NOT** be `null`
 - Updates `kotlin` to `1.9.21` [[#52]][52]
 - Updates `endians` to `0.2.0` [[#52]][52]
 - Drops support for the following deprecated targets:
     - `iosArm32`
     - `watchosX86`
     - `linuxArm32Hfp`
     - `linuxMips32`
     - `linuxMipsel32`
     - `mingwX86`
     - `wasm32`

## Version 0.3.0 (2023-06-28)
 - Fixes JPMS split packages [[#48]][48]
     - **API BREAKING CHANGES**
     - Package names were changed for `digest`, `mac`, and `xof` dependencies
     - Example:
         - `org.kotlincrypto.core.Digest` was moved to `org.kotlincrypto.core.digest.Digest`
         - `org.kotlincrypto.core.Mac` was moved to `org.kotlincrypto.core.mac.Mac`
         - `org.kotlincrypto.core.Xof` was moved to `org.kotlincrypto.core.xof.Xof`
 - See the [ANNOUNCEMENT][discussion-3] for more information on `0.3.0` release

## Version 0.2.7 (2023-06-09)
 - Fixes Android API 23 and below not calling `javax.crypto.MacSpi.engineReset`
   whenever `javax.crypto.Mac.doFinal` is invoked [[#46]][46]

## Version 0.2.6 (2023-06-08)
 - Fixes Android API 21-23 requiring a `Provider` in order to set
   `javax.crypto.Mac.spiImpl` when `javax.crypto.Mac.init` is
   invoked [[#44]][44]
 - Throw `InvalidKeyException` if `javax.crypto.Mac.init` is invoked [[#43]][43]
     - All `org.kotlincrypto.core.Mac` APIs are structured such that
       implementations always require a key as a constructor argument
       and are initialized immediately. As such, if a java caller
       attempts to re-initialize the `javax.crypto.Mac` with a different key
       they may assume future output produced is using the new key. This
       is **not** the case as `Kotlin Crypto` does not use a provider based
       architecture. A new, uninitialized `Mac` *cannot* be created.
     - Note that `Mac.init` is **not** available from `commonMain`. It is
       a remnant of bad API design requiring ability to lazily initialize
       things which `Kotlin Crypto` will **never** support as it leads
       to monolithic structures, instead of building on good abstractions.
       If `Mac.init` is required to be called, a wholly new instance of the
       `org.kotlincrypto.core.Mac` implementation needs to be instantiated
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

[discussion-3]: https://github.com/orgs/KotlinCrypto/discussions/3
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
[48]: https://github.com/KotlinCrypto/core/pull/48
[51]: https://github.com/KotlinCrypto/core/pull/51
[52]: https://github.com/KotlinCrypto/core/pull/52
[55]: https://github.com/KotlinCrypto/core/pull/55
[56]: https://github.com/KotlinCrypto/core/pull/56
[58]: https://github.com/KotlinCrypto/core/pull/58
[61]: https://github.com/KotlinCrypto/core/pull/61
[64]: https://github.com/KotlinCrypto/core/pull/64
[66]: https://github.com/KotlinCrypto/core/pull/66
[70]: https://github.com/KotlinCrypto/core/pull/70
[108]: https://github.com/KotlinCrypto/core/pull/108
[109]: https://github.com/KotlinCrypto/core/pull/109
[111]: https://github.com/KotlinCrypto/core/pull/111
[116]: https://github.com/KotlinCrypto/core/pull/116
[118]: https://github.com/KotlinCrypto/core/pull/118
[122]: https://github.com/KotlinCrypto/core/pull/122
[124]: https://github.com/KotlinCrypto/core/pull/124
