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
import org.kotlincrypto.core.internal.commonToString
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
public actual abstract class Digest private actual constructor(
    algorithm: String,
    blockSize: Int,
    digestLength: Int,
    state: DigestState?,
) : MessageDigest(algorithm),
    Algorithm,
    Cloneable,
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
    @Throws(IllegalArgumentException::class)
    protected actual constructor(
        algorithm: String,
        blockSize: Int,
        digestLength: Int
    ): this(
        algorithm = algorithm,
        blockSize = blockSize,
        digestLength = digestLength,
        state = null
    )

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
    protected actual constructor(
        state: DigestState
    ): this(
        algorithm = state.algorithm,
        blockSize = state.blockSize,
        digestLength = state.digestLength,
        state = state
    )

    public actual final override fun algorithm(): String = delegate.algorithm
    public actual fun blockSize(): Int = delegate.blockSize
    public actual fun digestLength(): Int = delegate.digestLength

    public actual final override fun update(input: Byte) { delegate.update(input) }
    public actual final override fun update(input: ByteArray) { delegate.update(input) }
    @Throws(IllegalArgumentException::class, IndexOutOfBoundsException::class)
    public actual final override fun update(input: ByteArray, offset: Int, len: Int) { delegate.update(input, offset, len) }

    public actual final override fun digest(): ByteArray = delegate.digest()
    public actual final override fun digest(input: ByteArray): ByteArray { delegate.update(input); return delegate.digest() }
    @Throws(IllegalArgumentException::class, DigestException::class)
    public final override fun digest(buf: ByteArray, offset: Int, len: Int): Int = super.digest(buf, offset, len)

    public actual final override fun reset() { delegate.reset() }

    protected final override fun engineGetDigestLength(): Int = digestLength

    protected final override fun engineUpdate(p0: Byte) { delegate.update(p0) }
    protected final override fun engineUpdate(input: ByteBuffer) { super.engineUpdate(input) }
    @Throws(IllegalArgumentException::class, IndexOutOfBoundsException::class)
    protected final override fun engineUpdate(p0: ByteArray, p1: Int, p2: Int) { delegate.update(p0, p1, p2) }

    protected final override fun engineDigest(): ByteArray = delegate.digest()
    @Throws(DigestException::class)
    protected final override fun engineDigest(buf: ByteArray, offset: Int, len: Int): Int = super.engineDigest(buf, offset, len)

    protected final override fun engineReset() { delegate.reset() }

    public actual final override fun equals(other: Any?): Boolean = other is Digest && other.delegate == delegate
    public actual final override fun hashCode(): Int = delegate.hashCode()
    public actual final override fun toString(): String = commonToString()

    public final override fun clone(): Any = copy()
    public actual final override fun copy(): Digest = copy(delegate.copy())
    protected actual abstract fun copy(state: DigestState): Digest

    protected actual abstract fun compress(buffer: ByteArray)
    protected actual abstract fun digest(bitLength: Long, bufferOffset: Int, buffer: ByteArray): ByteArray
    protected actual abstract fun resetDigest()
}
