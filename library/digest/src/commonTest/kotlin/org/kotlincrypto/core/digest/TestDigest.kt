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

import org.kotlincrypto.core.digest.internal.DigestState

class TestDigest: Digest {

    private val compress: (input: ByteArray, offset: Int) -> Unit
    private val finalize: (buffer: ByteArray, offset: Int) -> ByteArray
    private val reset: () -> Unit

    var compressions: Int = 0
        private set

    constructor(algorithm: String): this(algorithm, 64)

    constructor(
        algorithm: String = "TEST",
        blockSize: Int = 64,
        digestLength: Int = 32,
        compress: (input: ByteArray, offset: Int) -> Unit = { _, _ -> },
        digest: (buffer: ByteArray, offset: Int) -> ByteArray = { _, _ -> ByteArray(blockSize) },
        reset: () -> Unit = {},
    ): super(algorithm, blockSize, digestLength) {
        this.compress = compress
        this.finalize = digest
        this.reset = reset
    }

    private constructor(
        state: DigestState,
        compress: (input: ByteArray, offset: Int) -> Unit,
        digest: (buffer: ByteArray, offset: Int) -> ByteArray,
        reset: () -> Unit,
    ): super(state) {
        this.compress = compress
        this.finalize = digest
        this.reset = reset
    }

    override fun compressProtected(input: ByteArray, offset: Int) {
        compress.invoke(input, offset)
        compressions++
    }

    override fun digestProtected(buffer: ByteArray, offset: Int): ByteArray {
        return finalize.invoke(buffer, offset)
    }

    override fun resetProtected() {
        reset.invoke()
        compressions = 0
    }

    override fun copyProtected(state: DigestState): Digest {
        val d = TestDigest(state, compress, finalize, reset)
        d.compressions = compressions
        return d
    }
}
