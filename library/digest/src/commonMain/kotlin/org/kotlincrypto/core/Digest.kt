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
     * e.g.
     *
     *     class Md5: Digest {
     *
     *         private val x: IntArray = IntArray(16)
     *         private val state: IntArray = intArrayOf(
     *             1732584193,
     *             -271733879,
     *             -1732584194,
     *             271733878,
     *         )
     *
     *         constructor(): super("MD5", 64, 16)
     *         private constructor(state: DigestState, md5: Md5): super(state) {
     *             md5.x.copyInto(x)
     *             md5.state.copyInto(this.state)
     *         }
     *
     *         override fun copy(state: DigestState): Md5 = Md5(state, this)
     *
     *         // ...
     *     }
     *
     * @see [DigestState]
     * */
    @InternalKotlinCryptoApi
    protected constructor(state: DigestState)

    public final override fun algorithm(): String
    public fun blockSize(): Int
    public fun digestLength(): Int

    public final override fun update(input: Byte)
    public final override fun update(input: ByteArray)
    @Throws(IllegalArgumentException::class, IndexOutOfBoundsException::class)
    public final override fun update(input: ByteArray, offset: Int, len: Int)

    public fun digest(): ByteArray
    public fun digest(input: ByteArray): ByteArray

    public final override fun reset()

    public final override fun equals(other: Any?): Boolean
    public final override fun hashCode(): Int
    public final override fun toString(): String

    public final override fun copy(): Digest
    protected abstract fun copy(state: DigestState): Digest

    protected abstract fun compress(buffer: ByteArray)
    protected abstract fun digest(bitLength: Long, bufferOffset: Int, buffer: ByteArray): ByteArray
    protected abstract fun resetDigest()
}
