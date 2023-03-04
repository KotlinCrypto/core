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

public actual abstract class Digest private actual constructor(
    algorithm: String,
    blockSize: Int,
    digestLength: Int,
    state: DigestState?,
) : Algorithm,
    Cloneable<Digest>,
    Resettable,
    Updatable
{

    private val delegate = if (state != null) {
        DigestDelegate.instance(state, ::compress, ::digest, ::resetDigest)
    } else {
        DigestDelegate.instance(algorithm, blockSize, digestLength, ::compress, ::digest, ::resetDigest)
    }

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

    public actual fun digest(): ByteArray = delegate.digest()
    public actual fun digest(input: ByteArray): ByteArray { delegate.update(input); return delegate.digest() }

    public actual final override fun reset() { delegate.reset() }

    public actual final override fun equals(other: Any?): Boolean = other is Digest && other.delegate == delegate
    public actual final override fun hashCode(): Int = delegate.hashCode()
    public actual final override fun toString(): String = commonToString()

    public actual final override fun clone(): Digest {
        TODO("Not yet implemented")
    }

    protected actual abstract fun compress(buffer: ByteArray)
    protected actual abstract fun digest(bitLength: Long, bufferOffset: Int, buffer: ByteArray): ByteArray
    protected actual abstract fun resetDigest()
}
