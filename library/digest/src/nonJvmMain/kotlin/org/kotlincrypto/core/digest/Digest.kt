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
@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package org.kotlincrypto.core.digest

import org.kotlincrypto.core.*
import org.kotlincrypto.core.digest.internal.*
import org.kotlincrypto.core.digest.internal.Buffer.Companion.buf
import org.kotlincrypto.core.digest.internal.commonCheckArgs
import org.kotlincrypto.core.digest.internal.commonToString

/**
 * Core abstraction for Message Digest implementations.
 *
 * A Digest provides secure one-way hash functions that take in
 * arbitrary sized data and output a fixed-length hash value.
 *
 * Implementations of [Digest] should follow the Java naming
 * guidelines for [algorithm] which can be found at:
 *
 * https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html#messagedigest-algorithms
 * */
public actual abstract class Digest: Algorithm, Copyable<Digest>, Resettable, Updatable {

    private val algorithm: String
    private val digestLength: Int
    private val buf: Buffer
    private var bufOffs: Int
    private var compressCount: Long

    /**
     * Creates a new [Digest] for the specified parameters.
     *
     * @throws [IllegalArgumentException] when:
     *  - [algorithm] is blank
     *  - [blockSize] is less than or equal to 0
     *  - [blockSize] is not a factor of 8
     *  - [digestLength] is less than 0
     * */
    @InternalKotlinCryptoApi
    @Throws(IllegalArgumentException::class)
    protected actual constructor(algorithm: String, blockSize: Int, digestLength: Int) {
        this.buf = Buffer.initialize(algorithm, blockSize, digestLength)
        this.algorithm = algorithm
        this.digestLength = digestLength
        bufOffs = 0
        compressCount = 0L
    }

    /**
     * Creates a new [Digest] for the copied [state] of another [Digest]
     * instance.
     *
     * Implementors of [Digest] should have a private secondary constructor
     * that is utilized by its [copy] implementation.
     *
     * @see [DigestState]
     * */
    @InternalKotlinCryptoApi
    protected actual constructor(state: DigestState) {
        this.algorithm = state.algorithm
        this.digestLength = state.digestLength
        this.buf = state.buf()
        this.bufOffs = state.bufOffs
        this.compressCount = state.compressCount
    }

    public actual final override fun algorithm(): String = algorithm
    public actual fun blockSize(): Int = buf.value.size
    public actual fun digestLength(): Int = digestLength

    public actual override fun update(input: Byte) {
        updateDigest(input)
    }
    public actual final override fun update(input: ByteArray) {
        updateDigest(input, 0, input.size)
    }
    @Throws(IllegalArgumentException::class, IndexOutOfBoundsException::class)
    public actual final override fun update(input: ByteArray, offset: Int, len: Int) {
        input.commonCheckArgs(offset, len)
        updateDigest(input, offset, len)
    }

    public actual fun digest(): ByteArray = buf.commonDigest(
        bufOffs = bufOffs,
        compressCount = compressCount,
        digest = ::digest,
        reset = ::reset,
    )
    public actual fun digest(input: ByteArray): ByteArray {
        updateDigest(input, 0, input.size)
        return digest()
    }

    public actual final override fun reset() {
        buf.value.fill(0)
        bufOffs = 0
        compressCount = 0
        resetDigest()
    }

    public actual final override fun copy(): Digest = buf.toState(
        algorithm = algorithm,
        digestLength = digestLength,
        bufOffs = bufOffs,
        compressCount = compressCount,
    ).let { copy(it) }
    protected actual abstract fun copy(state: DigestState): Digest

    /**
     * Called whenever a full [blockSize] worth of bytes is available
     * to be processed, starting at index [offset] for the [input].
     * */
    protected actual abstract fun compress(input: ByteArray, offset: Int)
    protected actual abstract fun digest(bitLength: Long, bufferOffset: Int, buffer: ByteArray): ByteArray
    protected actual abstract fun resetDigest()

    /**
     * Protected, direct access to the [Digest]'s buffer. All external input
     * is directed here such that implementations can override and intercept
     * if necessary.
     * */
    protected actual open fun updateDigest(input: Byte) {
        buf.commonUpdate(
            input = input,
            bufOffsPlusPlus = bufOffs++,
            doCompression = { buf, offset ->
                compress(buf, offset)
                bufOffs = 0
                compressCount++
            },
        )
    }

    /**
     * Protected, direct access to the [Digest]'s buffer. All external input
     * is validated before being directed here such that implementations can
     * override and intercept if necessary.
     * */
    protected actual open fun updateDigest(input: ByteArray, offset: Int, len: Int) {
        buf.commonUpdate(
            input = input,
            offset = offset,
            len = len,
            bufOffs = bufOffs,
            bufOffsSet = { bufOffs = it },
            compress = ::compress,
            compressCountAdd = { compressCount += it },
        )
    }

    public actual final override fun equals(other: Any?): Boolean = other is Digest && other.buf == buf
    public actual final override fun hashCode(): Int = buf.hashCode()
    public actual final override fun toString(): String = commonToString()
}
