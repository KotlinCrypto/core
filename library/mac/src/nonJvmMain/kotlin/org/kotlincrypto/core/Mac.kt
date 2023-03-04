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

    public actual final override fun update(input: Byte) { engine.update(input) }
    public actual final override fun update(input: ByteArray) { engine.update(input) }
    public actual final override fun update(input: ByteArray, offset: Int, len: Int) { engine.update(input, offset, len) }

    public actual final override fun reset() { engine.reset() }

    public actual fun doFinal(): ByteArray = engine.doFinal()
    public actual fun doFinal(input: ByteArray): ByteArray { engine.update(input); return doFinal() }

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
     *
     * e.g.
     *
     *     public abstract class HMac: Mac {
     *
     *         @Throws(IllegalArgumentException::class)
     *         protected constructor(
     *             key: ByteArray,
     *             algorithm: String,
     *             digest: Digest,
     *         ): super(algorithm, Engine(key, digest))
     *
     *         protected constructor(
     *             algorithm: String,
     *             engine: HMac.Engine
     *         ): super(algorithm, engine)
     *
     *         protected class Engine: Mac.Engine {
     *
     *             private val state: State
     *
     *             @Throws(IllegalArgumentException::class)
     *             internal constructor(key: ByteArray, digest: Digest): super(key) {
     *                 digest.reset()
     *
     *                 val preparedKey = // ...
     *
     *                 state = State(
     *                     iKey = ByteArray(digest.blockSize()) { i -> preparedKey[i] xor 0x36 },
     *                     oKey = ByteArray(digest.blockSize()) { i -> preparedKey[i] xor 0x5C },
     *                     digest = digest,
     *                 )
     *
     *                 digest.update(state.iKey)
     *             }
     *
     *             private constructor(state: State): super(state) {
     *                 this.state = State(state.iKey, state.oKey, digest.copy())
     *             }
     *
     *             override fun copy(): Engine = Engine(state)
     *
     *             private inner class State(
     *                 val iKey: ByteArray,
     *                 val oKey: ByteArray,
     *                 val digest: Digest,
     *             ): Mac.Engine.State()
     *
     *             // ...
     *         }
     *     }
     * */
    protected actual abstract class Engine: Copyable<Engine>, Resettable, Updatable {

        private val hashCode: Int = Any().hashCode()

        /**
         * Initializes a new [Engine] with the provided [key].
         *
         * @throws [IllegalArgumentException] if [key] is empty.
         * */
        @Throws(IllegalArgumentException::class)
        public actual constructor(key: ByteArray) { require(key.isNotEmpty()) { "key cannot be empty" } }

        /**
         * Creates a new [Engine] for the copied [State]
         * */
        protected actual constructor(state: State)

        public actual abstract fun macLength(): Int
        public actual abstract fun doFinal(): ByteArray

        public actual final override fun equals(other: Any?): Boolean = other is Engine && other.hashCode == hashCode
        public actual final override fun hashCode(): Int = hashCode

        protected actual abstract inner class State
    }
}
