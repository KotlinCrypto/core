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

import org.kotlincrypto.core.internal.commonInit
import org.kotlincrypto.core.internal.commonToString

public actual abstract class Mac
@Throws(IllegalArgumentException::class)
protected actual constructor(
    private val algorithm: String,
    private val engine: Engine,
) : Algorithm,
    Resettable,
    Updatable
{

    init {
        commonInit(algorithm)
    }

    public actual final override fun algorithm(): String = algorithm
    public actual fun macLength(): Int = engine.macLength()

    public actual final override fun update(input: Byte) { engine.update(input) }
    public actual final override fun update(input: ByteArray) { engine.update(input) }
    public actual final override fun update(input: ByteArray, offset: Int, len: Int) { engine.update(input, offset, len) }

    public actual final override fun reset() { engine.reset() }

    public actual fun doFinal(): ByteArray = engine.doFinal()

    public actual final override fun equals(other: Any?): Boolean = other is Mac && other.engine == engine
    public actual final override fun hashCode(): Int = engine.hashCode()
    public actual final override fun toString(): String = commonToString()

    protected actual abstract class Engine
    @Throws(IllegalArgumentException::class)
    actual constructor(
        key: ByteArray
    ) : Resettable,
        Updatable
    {
        private val hashCode: Int

        init {
            require(key.isNotEmpty()) { "key cannot be empty" }
            hashCode = Any().hashCode()
        }

        public actual abstract fun macLength(): Int
        public actual abstract fun doFinal(): ByteArray

        public actual final override fun equals(other: Any?): Boolean = other is Engine && other.hashCode == hashCode
        public actual final override fun hashCode(): Int = hashCode
    }
}
