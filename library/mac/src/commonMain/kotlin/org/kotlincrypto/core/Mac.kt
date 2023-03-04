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
public expect abstract class Mac
@Throws(IllegalArgumentException::class)
protected constructor(
    algorithm: String,
    engine: Engine,
) : Algorithm,
    Copyable<Mac>,
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
    public fun doFinal(input: ByteArray): ByteArray

    public final override fun copy(): Mac
    protected abstract fun copy(engineCopy: Engine): Mac

    public final override fun equals(other: Any?): Boolean
    public final override fun hashCode(): Int
    public final override fun toString(): String

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
    protected abstract class Engine: Copyable<Engine>, Resettable, Updatable {

        /**
         * Initializes a new [Engine] with the provided [key].
         *
         * @throws [IllegalArgumentException] if [key] is empty.
         * */
        @Throws(IllegalArgumentException::class)
        public constructor(key: ByteArray)

        /**
         * Creates a new [Engine] for the copied [State]
         * */
        protected constructor(state: State)

        public abstract fun macLength(): Int
        public abstract fun doFinal(): ByteArray

        final override fun equals(other: Any?): Boolean
        final override fun hashCode(): Int

        protected abstract inner class State()
    }
}
