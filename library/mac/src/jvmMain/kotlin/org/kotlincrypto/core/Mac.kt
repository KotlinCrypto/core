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
import java.nio.ByteBuffer
import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.MacSpi

public actual abstract class Mac
@Throws(IllegalArgumentException::class)
protected actual constructor(
    algorithm: String,
    private val engine: Engine,
) : javax.crypto.Mac(engine, null, algorithm),
    Algorithm,
    Resettable,
    Updatable
{

    init {
        commonInit(algorithm)

        // So that 'javax.crypto.Mac.initialized' gets set to true
        super.init(null)
    }

    public actual final override fun algorithm(): String = algorithm
    public actual fun macLength(): Int = macLength

    public actual final override fun equals(other: Any?): Boolean = other is Mac && other.engine == engine
    public actual final override fun hashCode(): Int = engine.hashCode()
    public actual final override fun toString(): String = commonToString()

    protected actual abstract class Engine
    @Throws(IllegalArgumentException::class)
    actual constructor(
        key: ByteArray
    ) : MacSpi(),
        Resettable,
        Updatable
    {
        private val hashCode: Int

        init {
            require(key.isNotEmpty()) { "key cannot be empty" }
            hashCode = Any().hashCode()
        }

        public actual abstract fun macLength(): Int
        public actual abstract fun doFinal(): ByteArray

        protected final override fun engineUpdate(p0: Byte) { update(p0) }
        protected final override fun engineUpdate(input: ByteBuffer?) { super.engineUpdate(input) }
        protected final override fun engineUpdate(p0: ByteArray, p1: Int, p2: Int) { update(p0, p1, p2) }
        protected final override fun engineReset() { reset() }
        protected final override fun engineGetMacLength(): Int = macLength()
        protected final override fun engineInit(p0: Key?, p1: AlgorithmParameterSpec?) { /* no-op */ }
        protected final override fun engineDoFinal(): ByteArray = doFinal()

        public actual final override fun equals(other: Any?): Boolean = other is Engine && other.hashCode == hashCode
        public actual final override fun hashCode(): Int = hashCode
    }
}
