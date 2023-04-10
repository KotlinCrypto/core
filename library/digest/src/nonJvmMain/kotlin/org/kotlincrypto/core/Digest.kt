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
package org.kotlincrypto.core

import org.kotlincrypto.core.internal.DigestDelegate
import org.kotlincrypto.core.internal.DigestState
import org.kotlincrypto.core.internal.commonIfArgumentsValid
import org.kotlincrypto.core.internal.commonToString

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
public actual abstract class Digest private actual constructor(
    algorithm: String,
    blockSize: Int,
    digestLength: Int,
    state: DigestState?,
) : Algorithm,
    Copyable<Digest>,
    Resettable,
    Updatable
{

    private val delegate = if (state != null) {
        DigestDelegate.instance(state, ::compress, ::digest, ::resetDigest)
    } else {
        DigestDelegate.instance(algorithm, blockSize, digestLength, ::compress, ::digest, ::resetDigest)
    }

    /**
     * Creates a new [Digest] for the specified parameters.
     *
     * @throws [IllegalArgumentException] when:
     *  - [algorithm] is blank
     *  - [blockSize] is less than or equal to 0
     *  - [blockSize] is not a factor of 8
     *  - [digestLength] is less than or equal to 0
     * */
    @InternalKotlinCryptoApi
    @Throws(IllegalArgumentException::class)
    protected actual constructor(
        algorithm: String,
        blockSize: Int,
        digestLength: Int
    ): this(algorithm, blockSize, digestLength, null)

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
    protected actual constructor(
        state: DigestState
    ): this(state.algorithm, state.blockSize, state.digestLength, state)

    public actual final override fun algorithm(): String = delegate.algorithm
    public actual fun blockSize(): Int = delegate.blockSize
    public actual fun digestLength(): Int = delegate.digestLength

    public actual final override fun update(input: ByteArray) {
        update(0, input.size, input)
    }
    @Throws(IllegalArgumentException::class, IndexOutOfBoundsException::class)
    public actual final override fun update(input: ByteArray, offset: Int, len: Int) {
        input.commonIfArgumentsValid(offset, len) {
            update(offset, len, input)
        }
    }

    public actual override fun update(input: Byte) {
        delegate.update(input)
    }
    // Input arguments are always checked for validity before this is called
    protected actual open fun update(offset: Int, len: Int, input: ByteArray) {
        delegate.update(input, offset, len)
    }

    public actual fun digest(): ByteArray = delegate.digest()
    public actual fun digest(input: ByteArray): ByteArray {
        update(0, input.size, input)
        return delegate.digest()
    }

    public actual final override fun reset() { delegate.reset() }

    public actual final override fun equals(other: Any?): Boolean = other is Digest && other.delegate == delegate
    public actual final override fun hashCode(): Int = delegate.hashCode()
    public actual final override fun toString(): String = commonToString()

    public actual final override fun copy(): Digest = copy(delegate.copy())
    protected actual abstract fun copy(state: DigestState): Digest

    /**
     * Called whenever a full [blockSize] worth of bytes is available
     * to be processed, starting at index [offset] for the [input].
     * */
    protected actual abstract fun compress(input: ByteArray, offset: Int)
    protected actual abstract fun digest(bitLength: Long, bufferOffset: Int, buffer: ByteArray): ByteArray
    protected actual abstract fun resetDigest()
}
