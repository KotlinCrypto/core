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
import org.kotlincrypto.core.digest.internal.DigestState

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
public expect abstract class Digest: Algorithm, Copyable<Digest>, Resettable, Updatable {

    /**
     * Creates a new [Digest] for the specified parameters.
     *
     * @param [algorithm] See [Algorithm.algorithm]
     * @param [blockSize] See [Digest.blockSize]
     * @param [digestLength] See [Digest.digestLength]
     * @throws [IllegalArgumentException] when:
     *  - [algorithm] is blank
     *  - [blockSize] is less than or equal to 0
     *  - [blockSize] is not a factor of 8
     *  - [digestLength] is negative
     * */
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
     *     public class SHA256: Digest {
     *
     *         public constructor(): super("SHA-256", 64, 32) {
     *             // Initialize...
     *         }
     *         private constructor(thiz: SHA256, state: DigestState): super(state) {
     *             // Copy implementation details...
     *         }
     *         protected override fun copy(state: DigestState): Digest = SHA256(this, state)
     *         // ...
     *     }
     *
     * @see [DigestState]
     * */
    protected constructor(state: DigestState)

    /**
     * The number of byte blocks (in factors of 8) that the implementation
     * requires before one round of input processing is to occur. This value
     * is also representative of the digest's buffer size, and will always
     * be greater than 0.
     * */
    public fun blockSize(): Int

    /**
     * The number of bytes the implementation returns when [digest] is called.
     * */
    public fun digestLength(): Int

    // See Algorithm interface documentation
    public final override fun algorithm(): String

    // See Updatable interface documentation
    public final override fun update(input: Byte)
    // See Updatable interface documentation
    public final override fun update(input: ByteArray)
    // See Updatable interface documentation
    public final override fun update(input: ByteArray, offset: Int, len: Int)

    /**
     * Completes the computation, performing final operations and returning
     * the resultant array of bytes. The [Digest] is [reset] afterward.
     * */
    public fun digest(): ByteArray

    /**
     * Updates the instance with provided [input], then completes the computation,
     * performing final operations and returning the resultant array of bytes. The
     * [Digest] is [reset] afterward.
     * */
    public fun digest(input: ByteArray): ByteArray

    // See Resettable interface documentation
    public final override fun reset()

    // See Copyable interface documentation
    public final override fun copy(): Digest

    /**
     * The number of compressions this [Digest] has completed. Backing
     * variable is updated **after** each [compress] invocation, and
     * subsequently set to `0` upon [reset] invocation.
     * */
    protected fun compressions(): Long

    /**
     * Called by the public [copy] function which produces the
     * [DigestState] needed to create a wholly new instance.
     * */
    protected abstract fun copy(state: DigestState): Digest

    /**
     * Called whenever a full [blockSize] worth of bytes are available for processing,
     * starting at index [offset] for the provided [input]. Implementations **must not**
     * alter [input].
     * */
    protected abstract fun compress(input: ByteArray, offset: Int)

    /**
     * Called to complete the computation, providing any input that may be
     * buffered awaiting processing.
     *
     * @param [bitLength] The number of bits that have been processed, including
     *   those remaining in the [buffer]
     * @param [bufferOffset] The index at which the next input would be placed in
     *   the [buffer]
     * @param [buffer] Unprocessed input
     * */
    protected abstract fun digest(bitLength: Long, bufferOffset: Int, buffer: ByteArray): ByteArray

    /**
     * Optional override for implementations to intercept cleansed input before
     * being processed by the [Digest] abstraction.
     * */
    protected open fun updateDigest(input: Byte)

    /**
     * Optional override for implementations to intercept cleansed input before
     * being processed by the [Digest] abstraction. Parameters passed to this
     * function are always valid and have been checked for appropriateness.
     * */
    protected open fun updateDigest(input: ByteArray, offset: Int, len: Int)

    protected abstract fun resetDigest()

    /** @suppress */
    public final override fun equals(other: Any?): Boolean
    /** @suppress */
    public final override fun hashCode(): Int
    /** @suppress */
    public final override fun toString(): String
}
