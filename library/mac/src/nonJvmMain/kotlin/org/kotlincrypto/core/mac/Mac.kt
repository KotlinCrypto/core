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
package org.kotlincrypto.core.mac

import org.kotlincrypto.core.*
import org.kotlincrypto.core.mac.internal.commonInit
import org.kotlincrypto.core.mac.internal.commonToString

/**
 * Core abstraction for Message Authentication Code implementations.
 *
 * A MAC provides a way to check the integrity of information transmitted
 * over or stored in an unreliable medium, based on a secret (key). Typically,
 * message authentication codes are used between two parties that share a
 * secret (key) in order to validate information transmitted between these
 * parties.
 *
 * Implementations of [Mac] should follow the Java naming guidelines for
 * [algorithm] which can be found at:
 *
 * https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html#mac-algorithms
 *
 * @see [Engine]
 * @throws [IllegalArgumentException] if [algorithm] is blank
 * */
public actual abstract class Mac
@InternalKotlinCryptoApi
@Throws(IllegalArgumentException::class)
protected actual constructor(
    private val algorithm: String,
    private val engine: Engine,
) : Algorithm,
    Copyable<Mac>,
    Resettable,
    Updatable
{

    init {
        commonInit(algorithm)
    }

    public actual final override fun algorithm(): String = algorithm
    public actual fun macLength(): Int = engine.macLength()

    public actual final override fun update(input: Byte) {
        engine.update(input)
    }
    public actual final override fun update(input: ByteArray) {
        engine.update(input, 0, input.size)
    }
    public actual final override fun update(input: ByteArray, offset: Int, len: Int) {
        if (offset < 0 || len < 0 || offset > input.size - len) throw IllegalArgumentException("Bad arguments")
        engine.update(input, offset, len)
    }

    public actual fun doFinal(): ByteArray {
        val final = engine.doFinal()
        engine.reset()
        return final
    }
    public actual fun doFinal(input: ByteArray): ByteArray {
        engine.update(input, 0, input.size)
        return doFinal()
    }

    public actual final override fun reset() {
        engine.reset()
    }

    public actual final override fun copy(): Mac = copy(engine.copy())
    protected actual abstract fun copy(engineCopy: Engine): Mac

    public actual final override fun equals(other: Any?): Boolean = other is Mac && other.engine == engine
    public actual final override fun hashCode(): Int = engine.hashCode()
    public actual final override fun toString(): String = commonToString()

    /**
     * Core abstraction for powering a [Mac] implementation.
     *
     * Implementors of [Engine] **must** initialize the instance with the
     * provided key parameter.
     * */
    protected actual abstract class Engine: Copyable<Engine>, Resettable, Updatable {

        private val hashCode: Int = Any().hashCode()

        /**
         * Initializes a new [Engine] with the provided [key].
         *
         * @throws [IllegalArgumentException] if [key] is empty.
         * */
        @InternalKotlinCryptoApi
        @Throws(IllegalArgumentException::class)
        public actual constructor(key: ByteArray) { require(key.isNotEmpty()) { "key cannot be empty" } }

        /**
         * Creates a new [Engine] for the copied [State]
         * */
        @InternalKotlinCryptoApi
        protected actual constructor(state: State)

        public actual abstract fun macLength(): Int
        public actual abstract fun doFinal(): ByteArray

        public actual override fun update(input: ByteArray) { update(input, 0, input.size) }

        public actual final override fun equals(other: Any?): Boolean = other is Engine && other.hashCode == hashCode
        public actual final override fun hashCode(): Int = hashCode

        protected actual abstract inner class State
    }
}
