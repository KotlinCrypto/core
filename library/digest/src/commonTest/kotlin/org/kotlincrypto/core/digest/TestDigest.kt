/*
 * Copyright (c) 2023 Matthew Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package org.kotlincrypto.core.digest

import org.kotlincrypto.core.InternalKotlinCryptoApi
import org.kotlincrypto.core.digest.internal.DigestState

@Suppress("UnnecessaryOptInAnnotation")
@OptIn(InternalKotlinCryptoApi::class)
class TestDigest: Digest {

    private val compress: (input: ByteArray, offset: Int) -> Unit
    private val finalize: (Long, Int, ByteArray) -> ByteArray
    private val reset: () -> Unit

    constructor(algorithm: String): this(algorithm, 64)

    constructor(
        algorithm: String = "TEST",
        blockSize: Int = 64,
        digestLength: Int = 32,
        compress: (input: ByteArray, offset: Int) -> Unit = { _, _ -> },
        digest: (bitLength: Long, bufferOffset: Int, buffer: ByteArray) -> ByteArray = { _, _, _ -> ByteArray(blockSize) },
        reset: () -> Unit = {},
    ): super(algorithm, blockSize, digestLength) {
        this.compress = compress
        this.finalize = digest
        this.reset = reset
    }

    private constructor(
        state: DigestState,
        compress: (input: ByteArray, offset: Int) -> Unit,
        digest: (bitLength: Long, bufferOffset: Int, buffer: ByteArray) -> ByteArray,
        reset: () -> Unit,
    ): super(state) {
        this.compress = compress
        this.finalize = digest
        this.reset = reset
    }

    override fun compress(input: ByteArray, offset: Int) {
        compress.invoke(input, offset)
    }

    override fun digest(bitLength: Long, bufferOffset: Int, buffer: ByteArray): ByteArray {
        return finalize.invoke(bitLength, bufferOffset, buffer)
    }

    override fun resetDigest() {
        reset.invoke()
    }

    override fun copy(state: DigestState): Digest {
        return TestDigest(state, compress, finalize, reset)
    }
}
