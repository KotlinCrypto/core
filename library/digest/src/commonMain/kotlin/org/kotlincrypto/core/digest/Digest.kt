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
     * Creates a new [Digest] from [other], copying its state.
     *
     * Implementors of [Digest] should have a private secondary constructor
     * that is utilized by its [copy] implementation.
     *
     * e.g.
     *
     *     public class SHA256: Digest {
     *
     *         // ...
     *
     *         private constructor(other: SHA256): super(other) {
     *             // Copy implementation details...
     *         }
     *
     *         // Notice the updated return type
     *         public override fun copy(): SHA256 = SHA256(this)
     *
     *         // ...
     *     }
     * */
    protected constructor(other: Digest)

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
     * Updates the instance with provided [input] then completes the computation,
     * performing final operations and returning the resultant array of bytes. The
     * [Digest] is [reset] afterward.
     * */
    public fun digest(input: ByteArray): ByteArray

    /**
     * Completes the computation, performing final operations and placing the
     * resultant bytes into the provided [dest] array starting at index [destOffset].
     * The [Digest] is [reset] afterward.
     *
     * @return The number of bytes put into [dest] (i.e. the [digestLength])
     * @throws [IndexOutOfBoundsException] if [destOffset] is inappropriate
     * @throws [ShortBufferException] if [digestLength] number of bytes are unable
     *   to fit into [dest] for provided [destOffset]
     * */
    public fun digestInto(dest: ByteArray, destOffset: Int): Int

    // See Resettable interface documentation
    public final override fun reset()

    /**
     * Called whenever a full [blockSize] worth of bytes are available for processing,
     * starting at index [offset] for the provided [input]. Implementations **must not**
     * alter [input].
     * */
    protected abstract fun compressProtected(input: ByteArray, offset: Int)

    /**
     * Called to complete the computation, providing any input that may be buffered
     * and awaiting processing.
     *
     * **NOTE:** The buffer from [bufPos] to the end will always be zeroed out to clear
     * any potentially stale input left over from a previous state.
     *
     * @param [buf] Unprocessed input
     * @param [bufPos] The index at which the **next** input would be placed into [buf]
     * */
    protected abstract fun digestProtected(buf: ByteArray, bufPos: Int): ByteArray

    /**
     * Called to complete the computation, providing any input that may be buffered
     * and awaiting processing.
     *
     * Implementations should override this addition to the API for performance reasons.
     * If overridden, `super.digestIntoProtected` should **not** be called.
     *
     * **NOTE:** The buffer from [bufPos] to the end will always be zeroed out to clear
     * any potentially stale input left over from a previous state.
     *
     * **NOTE:** The public [digestInto] function always checks [dest] for capacity of
     * [digestLength], starting at [destOffset], before calling this function.
     *
     * @param [dest] The array to place resultant bytes
     * @param [destOffset] The index to begin placing bytes into [dest]
     * @param [buf] Unprocessed input
     * @param [bufPos] The index at which the **next** input would be placed into [buf]
     * */
    protected open fun digestIntoProtected(dest: ByteArray, destOffset: Int, buf: ByteArray, bufPos: Int)

    /**
     * Optional override for implementations to intercept cleansed input before
     * being processed by the [Digest] abstraction.
     * */
    protected open fun updateProtected(input: Byte)

    /**
     * Optional override for implementations to intercept cleansed input before
     * being processed by the [Digest] abstraction. Parameters passed to this
     * function are always valid and have been checked for appropriateness.
     * */
    protected open fun updateProtected(input: ByteArray, offset: Int, len: Int)

    protected abstract fun resetProtected()

    /** @suppress */
    public final override fun equals(other: Any?): Boolean
    /** @suppress */
    public final override fun hashCode(): Int
    /** @suppress */
    public final override fun toString(): String
}
