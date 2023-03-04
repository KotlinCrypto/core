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

    @Throws(IllegalArgumentException::class)
    protected constructor(algorithm: String, blockSize: Int, digestLength: Int)
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
