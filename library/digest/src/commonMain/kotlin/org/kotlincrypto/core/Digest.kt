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

import kotlin.jvm.JvmField

public expect abstract class Digest
@Throws(IllegalArgumentException::class)
protected constructor(
    algorithm: String,
    blockSize: Int,
    digestLength: Int,
) : Algorithm,
    Cloneable<Digest>,
    Resettable,
    Updatable
{
    @JvmField
    public val blockSize: Int
    @JvmField
    public val digestLength: Int

    public final override fun update(input: Byte)
    public final override fun update(input: ByteArray)
    @Throws(IllegalArgumentException::class, IndexOutOfBoundsException::class)
    public final override fun update(input: ByteArray, offset: Int, len: Int)

    public fun digest(): ByteArray
    public fun digest(input: ByteArray): ByteArray

    public final override fun reset()

    public final override fun algorithm(): String

    public final override fun equals(other: Any?): Boolean
    public final override fun hashCode(): Int
    public final override fun toString(): String

    public final override fun clone(): Digest

    protected abstract fun compress(buffer: ByteArray)
    protected abstract fun digest(bitLength: Long, bufferOffset: Int, buffer: ByteArray): ByteArray
    protected abstract fun resetDigest()
}
