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
@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

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
public actual abstract class Mac: Algorithm, Copyable<Mac>, Resettable, Updatable {

    private val algorithm: String
    private val engine: Engine

    /**
     * Creates a new [Mac] for the specified parameters.
     *
     * @param [algorithm] See [Algorithm.algorithm]
     * @param [engine] See [Engine]
     * @throws [IllegalArgumentException] when:
     *  - [algorithm] is blank
     * */
    @Throws(IllegalArgumentException::class)
    protected actual constructor(algorithm: String, engine: Engine) {
        commonInit(algorithm)
        this.algorithm = algorithm
        this.engine = engine
    }

    /**
     * Creates a new [Mac] from [other], copying its [Engine] and state.
     *
     * Implementors of [Mac] should have a private secondary constructor
     * that is utilized by its [copy] implementation.
     *
     * e.g.
     *
     *     public class HmacSHA256: Mac {
     *
     *         // ...
     *
     *         private constructor(other: HmacSHA256): super(other) {
     *             // Copy implementation details...
     *         }
     *
     *         // Notice the updated return type
     *         public override fun copy(): HmacSHA256 = HmacSHA256(this)
     *
     *         // ...
     *     }
     * */
    protected actual constructor(other: Mac): this(other.algorithm, other.engine.copy())

    /**
     * The number of bytes the implementation returns when [doFinal] is called.
     * */
    public actual fun macLength(): Int = engine.macLength()

    // See Algorithm interface documentation
    public actual final override fun algorithm(): String = algorithm

    // See Updatable interface documentation
    public actual final override fun update(input: Byte) {
        engine.update(input)
    }
    // See Updatable interface documentation
    public actual final override fun update(input: ByteArray) {
        engine.update(input, 0, input.size)
    }
    // See Updatable interface documentation
    public actual final override fun update(input: ByteArray, offset: Int, len: Int) {
        if (offset < 0 || len < 0 || offset > input.size - len) throw IllegalArgumentException("Bad arguments")
        engine.update(input, offset, len)
    }

    /**
     * Completes the computation, performing final operations and returning
     * the resultant array of bytes. The [Mac] is [reset] afterward.
     * */
    public actual fun doFinal(): ByteArray {
        val final = engine.doFinal()
        engine.reset()
        return final
    }

    /**
     * Updates the instance with provided [input], then completes the computation,
     * performing final operations and returning the resultant array of bytes. The
     * [Mac] is [reset] afterward.
     * */
    public actual fun doFinal(input: ByteArray): ByteArray {
        engine.update(input, 0, input.size)
        return doFinal()
    }

    // See Resettable interface documentation
    public actual final override fun reset() {
        engine.reset()
    }

    /** @suppress */
    public actual final override fun equals(other: Any?): Boolean = other is Mac && other.engine == engine
    /** @suppress */
    public actual final override fun hashCode(): Int = engine.hashCode()
    /** @suppress */
    public actual final override fun toString(): String = commonToString()

    /**
     * Core abstraction for powering a [Mac] implementation.
     *
     * Implementors of [Engine] **must** initialize the instance with the
     * provided key parameter.
     * */
    protected actual abstract class Engine: Copyable<Engine>, Resettable, Updatable {

        /**
         * Initializes a new [Engine] with the provided [key].
         *
         * @throws [IllegalArgumentException] if [key] is empty.
         * */
        @Throws(IllegalArgumentException::class)
        public actual constructor(key: ByteArray) {
            require(key.isNotEmpty()) { "key cannot be empty" }
        }

        /**
         * Creates a new [Engine] from [other], copying its state.
         * */
        protected actual constructor(other: Engine)

        /**
         * The number of bytes the implementation returns when [doFinal] is called.
         * */
        public actual abstract fun macLength(): Int

        /**
         * Completes the computation, performing final operations and returning
         * the resultant array of bytes. The [Engine] is [reset] afterward.
         * */
        public actual abstract fun doFinal(): ByteArray

        // See Updatable interface documentation
        public actual override fun update(input: ByteArray) { update(input, 0, input.size) }

        private val code = Any()

        /** @suppress */
        public actual final override fun equals(other: Any?): Boolean = other is Engine && other.hashCode() == hashCode()
        /** @suppress */
        public actual final override fun hashCode(): Int = code.hashCode()
    }
}
