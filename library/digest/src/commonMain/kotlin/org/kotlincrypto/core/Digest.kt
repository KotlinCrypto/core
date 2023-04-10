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

import org.kotlincrypto.core.internal.DigestState

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
public expect abstract class Digest private constructor(
    algorithm: String,
    blockSize: Int,
    digestLength: Int,
    state: DigestState?,
) : Algorithm,
    Copyable<Digest>,
    Resettable,
    Updatable
{

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
    protected constructor(algorithm: String, blockSize: Int, digestLength: Int)

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
    protected constructor(state: DigestState)

    public final override fun algorithm(): String
    public fun blockSize(): Int
    public fun digestLength(): Int

    public final override fun update(input: ByteArray)
    @Throws(IllegalArgumentException::class, IndexOutOfBoundsException::class)
    public final override fun update(input: ByteArray, offset: Int, len: Int)

    public override fun update(input: Byte)
    // Input arguments are always checked for validity before this is called
    protected open fun update(offset: Int, len: Int, input: ByteArray)

    public fun digest(): ByteArray
    public fun digest(input: ByteArray): ByteArray

    public final override fun reset()

    public final override fun equals(other: Any?): Boolean
    public final override fun hashCode(): Int
    public final override fun toString(): String

    public final override fun copy(): Digest
    protected abstract fun copy(state: DigestState): Digest

    /**
     * Called whenever a full [blockSize] worth of bytes is available
     * to be processed, starting at index [offset] for the [input].
     * */
    protected abstract fun compress(input: ByteArray, offset: Int)
    protected abstract fun digest(bitLength: Long, bufferOffset: Int, buffer: ByteArray): ByteArray
    protected abstract fun resetDigest()
}
