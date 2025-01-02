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
import org.kotlincrypto.core.digest.internal.commonCheckArgs
import org.kotlincrypto.core.digest.internal.commonToString
import java.nio.ByteBuffer
import java.security.DigestException
import java.security.MessageDigest

/**
 * Core abstraction for Message Digest implementations. Extends
 * Java's [MessageDigest] for compatibility.
 *
 * A Digest provides secure one-way hash functions that take in
 * arbitrary sized data and output a fixed-length hash value.
 *
 * Implementations of [Digest] should follow the Java naming
 * guidelines for [algorithm] which can be found at:
 *
 * https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html#messagedigest-algorithms
 * */
public actual abstract class Digest: MessageDigest, Algorithm, Cloneable, Copyable<Digest>, Resettable, Updatable {

    private val digestLength: Int
    private val buf: Buffer
    private var bufOffs: Int

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
    protected actual constructor(algorithm: String, blockSize: Int, digestLength: Int): super(algorithm) {
        this.buf = Buffer.initialize(algorithm, blockSize, digestLength)
        this.digestLength = digestLength
        this.bufOffs = 0
    }

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
    protected actual constructor(other: Digest): super(other.algorithm) {
        this.digestLength = other.digestLength
        this.buf = other.buf.copy()
        this.bufOffs = other.bufOffs
    }

    /**
     * The number of byte blocks (in factors of 8) that the implementation
     * requires before one round of input processing is to occur. This value
     * is also representative of the digest's buffer size, and will always
     * be greater than 0.
     * */
    public actual fun blockSize(): Int = buf.value.size

    /**
     * The number of bytes the implementation returns when [digest] is called.
     * */
    public actual fun digestLength(): Int = digestLength

    // See Algorithm interface documentation
    public actual final override fun algorithm(): String = algorithm

    // See Updatable interface documentation
    public actual final override fun update(input: Byte) {
        updateProtected(input)
    }
    // See Updatable interface documentation
    public actual final override fun update(input: ByteArray) {
        updateProtected(input, 0, input.size)
    }
    // See Updatable interface documentation
    public actual final override fun update(input: ByteArray, offset: Int, len: Int) {
        input.commonCheckArgs(offset, len)
        updateProtected(input, offset, len)
    }

    /**
     * Completes the computation, performing final operations and returning
     * the resultant array of bytes. The [Digest] is [reset] afterward.
     * */
    public actual final override fun digest(): ByteArray {
        val final = digestProtected(buf.value, bufOffs)
        reset()
        return final
    }

    /**
     * Updates the instance with provided [input], then completes the computation,
     * performing final operations and returning the resultant array of bytes. The
     * [Digest] is [reset] afterward.
     * */
    public actual final override fun digest(input: ByteArray): ByteArray {
        updateProtected(input, 0, input.size)
        return digest()
    }

    // See Resettable interface documentation
    public actual final override fun reset() {
        buf.value.fill(0)
        bufOffs = 0
        resetProtected()
    }

    /**
     * Called whenever a full [blockSize] worth of bytes are available for processing,
     * starting at index [offset] for the provided [input]. Implementations **must not**
     * alter [input].
     * */
    protected actual abstract fun compressProtected(input: ByteArray, offset: Int)

    /**
     * Called to complete the computation, providing any input that may be
     * buffered awaiting processing.
     *
     * @param [buffer] Unprocessed input
     * @param [offset] The index at which the next input would be placed in the [buffer]
     * */
    protected actual abstract fun digestProtected(buffer: ByteArray, offset: Int): ByteArray

    /**
     * Optional override for implementations to intercept cleansed input before
     * being processed by the [Digest] abstraction.
     * */
    protected actual open fun updateProtected(input: Byte) {
        buf.commonUpdate(
            input = input,
            bufOffsPlusPlus = bufOffs++,
            bufOffsSet = { bufOffs = it },
            compressProtected = ::compressProtected,
        )
    }

    /**
     * Optional override for implementations to intercept cleansed input before
     * being processed by the [Digest] abstraction. Parameters passed to this
     * function are always valid and have been checked for appropriateness.
     * */
    protected actual open fun updateProtected(input: ByteArray, offset: Int, len: Int) {
        buf.commonUpdate(
            input = input,
            offset = offset,
            len = len,
            bufOffs = bufOffs,
            bufOffsSet = { bufOffs = it },
            compressProtected = ::compressProtected,
        )
    }

    protected actual abstract fun resetProtected()

    // MessageDigest
    /** @suppress */
    @Throws(IllegalArgumentException::class, DigestException::class)
    public final override fun digest(buf: ByteArray, offset: Int, len: Int): Int = super.digest(buf, offset, len)
    /** @suppress */
    public final override fun clone(): Any = copy()

    // MessageDigestSpi
    /** @suppress */
    protected final override fun engineGetDigestLength(): Int = digestLength
    /** @suppress */
    protected final override fun engineUpdate(p0: Byte) { updateProtected(p0) }
    /** @suppress */
    protected final override fun engineUpdate(input: ByteBuffer) { super.engineUpdate(input) }
    /** @suppress */
    @Throws(IllegalArgumentException::class, IndexOutOfBoundsException::class)
    protected final override fun engineUpdate(p0: ByteArray, p1: Int, p2: Int) { update(p0, p1, p2) }
    /** @suppress */
    protected final override fun engineDigest(): ByteArray = digest()
    /** @suppress */
    @Throws(DigestException::class)
    protected final override fun engineDigest(buf: ByteArray, offset: Int, len: Int): Int {
        return super.engineDigest(buf, offset, len)
    }
    /** @suppress */
    protected final override fun engineReset() { reset() }

    /** @suppress */
    public actual final override fun equals(other: Any?): Boolean = other is Digest && other.buf == buf
    /** @suppress */
    public actual final override fun hashCode(): Int = buf.hashCode()
    /** @suppress */
    public actual final override fun toString(): String = commonToString()
}
