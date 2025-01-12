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
 * */
public expect abstract class Mac: Algorithm, Copyable<Mac>, Resettable, Updatable {

    /**
     * Creates a new [Mac] for the specified parameters.
     *
     * @param [algorithm] See [Algorithm.algorithm]
     * @param [engine] See [Engine]
     * @throws [IllegalArgumentException] when:
     *  - [algorithm] is blank
     * */
    @Throws(IllegalArgumentException::class)
    protected constructor(algorithm: String, engine: Engine)

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
    protected constructor(other: Mac)

    /**
     * The number of bytes the implementation returns when [doFinal] is called.
     * */
    public fun macLength(): Int

    // See Algorithm interface documentation
    public final override fun algorithm(): String

    // See Updatable interface documentation
    public final override fun update(input: Byte)
    // See Updatable interface documentation
    public final override fun update(input: ByteArray)
    // See Updatable interface documentation
    public final override fun update(input: ByteArray, offset: Int, len: Int)

    /**
     * Completes the computation, performing final operations and returning
     * the resultant array of bytes. The [Mac] is [reset] afterward.
     * */
    public fun doFinal(): ByteArray

    /**
     * Updates the instance with provided [input], then completes the computation,
     * performing final operations and returning the resultant array of bytes. The
     * [Mac] is [reset] afterward.
     * */
    public fun doFinal(input: ByteArray): ByteArray

    // See Resettable interface documentation
    public final override fun reset()

    /**
     * Resets the [Mac] and will reinitialize it with the provided key.
     *
     * This is useful if wanting to zero out the key before de-referencing.
     *
     * @see [clearKey]
     * @throws [IllegalArgumentException] if [newKey] is empty, or of a length
     *   inappropriate for the [Mac] implementation.
     * */
    public fun reset(newKey: ByteArray)

    /**
     * Helper function that will call [reset] with a blank key in order
     * to zero it out.
     * */
    public fun clearKey()

    /**
     * Core abstraction for powering a [Mac] implementation.
     *
     * Implementors of [Engine] **must** initialize the instance with the
     * provided key parameter.
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
         * Creates a new [Engine] from [other], copying its state.
         * */
        protected constructor(other: Engine)

        /**
         * The number of bytes the implementation returns when [doFinal] is called.
         * */
        public abstract fun macLength(): Int

        /**
         * Completes the computation, performing final operations and returning
         * the resultant array of bytes. The [Engine] is [reset] afterward.
         * */
        public abstract fun doFinal(): ByteArray

        // See Updatable interface documentation
        public override fun update(input: ByteArray)

        /**
         * Resets the [Engine] and will reinitialize it with the provided key.
         *
         * **NOTE:** [newKey] is checked to be non-empty by the [Mac] abstraction
         * before passing it here. Implementations should ensure any old key material
         * is zeroed out.
         *
         * @throws [IllegalArgumentException] if [newKey] is a length inappropriate
         *   for the [Mac] implementation.
         * */
        @Throws(IllegalArgumentException::class)
        public abstract fun reset(newKey: ByteArray)

        /** @suppress */
        final override fun equals(other: Any?): Boolean
        /** @suppress */
        final override fun hashCode(): Int
    }

    /** @suppress */
    public final override fun equals(other: Any?): Boolean
    /** @suppress */
    public final override fun hashCode(): Int
    /** @suppress */
    public final override fun toString(): String
}
