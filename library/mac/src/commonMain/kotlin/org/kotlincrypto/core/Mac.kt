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

public expect abstract class Mac
@Throws(IllegalArgumentException::class)
protected constructor(
    algorithm: String,
    engine: Engine,
) : Algorithm,
    Resettable,
    Updatable
{

    public final override fun algorithm(): String
    public fun macLength(): Int

    public final override fun update(input: Byte)
    public final override fun update(input: ByteArray)
    @Throws(IllegalArgumentException::class, IndexOutOfBoundsException::class)
    public final override fun update(input: ByteArray, offset: Int, len: Int)

    public final override fun reset()

    public fun doFinal(): ByteArray

    final override fun equals(other: Any?): Boolean
    final override fun hashCode(): Int
    final override fun toString(): String

    protected abstract class Engine
    @Throws(IllegalArgumentException::class)
    constructor (
        key: ByteArray
    ) : Resettable,
        Updatable
    {
        public abstract fun macLength(): Int
        public abstract fun doFinal(): ByteArray

        final override fun equals(other: Any?): Boolean
        final override fun hashCode(): Int
    }
}
