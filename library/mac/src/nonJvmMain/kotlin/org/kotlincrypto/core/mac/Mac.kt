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
import org.kotlincrypto.core.mac.internal.*
import org.kotlincrypto.error.ShortBufferException

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
        input.commonCheckArgs(offset, len, onOutOfBounds = ::IllegalArgumentException)
        engine.update(input, offset, len)
    }

    /**
     * Completes the computation, performing final operations and returning
     * the resultant array of bytes. The [Mac] is [reset] afterward.
     * */
    public actual fun doFinal(): ByteArray {
        val final = engine.doFinal()
        if (engine.resetOnDoFinal) engine.reset()
        return final
    }

    /**
     * Updates the instance with provided [input] then completes the computation,
     * performing final operations and returning the resultant array of bytes. The
     * [Mac] is [reset] afterward.
     * */
    public actual fun doFinal(input: ByteArray): ByteArray {
        engine.update(input, 0, input.size)
        return doFinal()
    }

    /**
     * Completes the computation, performing final operations and placing the
     * resultant bytes into the provided [dest] array starting at index [destOffset].
     * The [Mac] is [reset] afterward.
     *
     * @return The number of bytes put into [dest] (i.e. the [macLength])
     * @throws [IndexOutOfBoundsException] if [destOffset] is inappropriate
     * @throws [ShortBufferException] if [macLength] number of bytes are unable
     *   to fit into [dest] for provided [destOffset]
     * */
    public actual fun doFinalInto(dest: ByteArray, destOffset: Int): Int = commonDoFinalInto(
        dest = dest,
        destOffset = destOffset,
        engineResetOnDoFinal = engine.resetOnDoFinal,
        engineDoFinalInto = engine::doFinalInto,
        engineReset = engine::reset
    )

    // See Resettable interface documentation
    public actual final override fun reset() { engine.reset() }

    /**
     * Resets the [Mac] and will reinitialize it with the provided key.
     *
     * This is useful if wanting to zero out the key before de-referencing.
     *
     * @see [clearKey]
     * @throws [IllegalArgumentException] if [newKey] is empty, or of a length
     *   inappropriate for the [Mac] implementation.
     * */
    public actual fun reset(newKey: ByteArray) {
        require(newKey.isNotEmpty()) { "newKey cannot be empty" }
        engine.reset(newKey)
    }

    /**
     * Helper function that will call [reset] with a blank key in order
     * to zero it out.
     * */
    public actual fun clearKey() { commonClearKey(engine::reset) }

    /**
     * Core abstraction for powering a [Mac] implementation.
     *
     * Implementors of [Engine] **must** initialize the instance with the
     * provided key parameter.
     * */
    protected actual abstract class Engine: Copyable<Engine>, Resettable, Updatable {

        /**
         * Most [Mac.Engine] are backed by a `Digest`, whereby calling [reset] after
         * [doFinal] will cause a double reset (because `Digest.digest` does this inherently).
         * By setting this value to `false`, [Engine.reset] will **not** be called whenever
         * [doFinal] gets invoked.
         *
         * **NOTE:** Implementations taking ownership of the automatic reset functionality
         * by setting this to `false` must ensure that whatever re-initialization steps were
         * taken in their [Engine.reset] function body are executed before their [doFinal]
         * and [doFinalInto] implementations return.
         * */
        public actual val resetOnDoFinal: Boolean

        /**
         * Initializes a new [Engine] with the provided [key] with the default [resetOnDoFinal]
         * value of `true` (i.e. [Engine.reset] will be called automatically after [Engine.doFinal]
         * or [Engine.doFinalInto] have been invoked).
         *
         * @param [key] The key that this [Engine] instance will use to apply its function to
         * @throws [IllegalArgumentException] if [key] is empty
         * */
        @Throws(IllegalArgumentException::class)
        public actual constructor(key: ByteArray): this(key, resetOnDoFinal = true)

        /**
         * Initializes a new [Engine] with the provided [key] and [resetOnDoFinal] configuration.
         *
         * @param [key] the key that this [Engine] instance will use to apply its function to
         * @param [resetOnDoFinal] See [Engine.resetOnDoFinal] documentation
         * @throws [IllegalArgumentException] if [key] is empty
         * */
        @Throws(IllegalArgumentException::class)
        public actual constructor(key: ByteArray, resetOnDoFinal: Boolean) {
            require(key.isNotEmpty()) { "key cannot be empty" }
            this.resetOnDoFinal = resetOnDoFinal
        }

        /**
         * Creates a new [Engine] from [other], copying its state.
         * */
        protected actual constructor(other: Engine) {
            this.resetOnDoFinal = other.resetOnDoFinal
        }

        /**
         * The number of bytes the implementation returns when [doFinal] is called.
         * */
        public actual abstract fun macLength(): Int

        // See Updatable interface documentation
        public actual override fun update(input: ByteArray) { update(input, 0, input.size) }

        /**
         * Completes the computation, performing final operations and returning
         * the resultant array of bytes. The [Engine] is [reset] afterward.
         * */
        public actual abstract fun doFinal(): ByteArray

        /**
         * Called to complete the computation, performing final operations and placing
         * the resultant bytes into the provided [dest] array starting at index [destOffset].
         * The [Engine] is [reset] afterward.
         *
         * Implementations should override this addition to the API for performance reasons.
         * If overridden, `super.doFinalInto` should **not** be called.
         *
         * **NOTE:** The public [Mac.doFinalInto] function always checks [dest] for capacity
         * of [macLength], starting at [destOffset], before calling this function.
         *
         * @param [dest] The array to place resultant bytes
         * @param [destOffset] The index to begin placing bytes into [dest]
         * */
        public actual open fun doFinalInto(dest: ByteArray, destOffset: Int) {
            // Default implementation. Extenders of Mac.Engine should override.
            val result = doFinal()
            result.copyInto(dest, destOffset)
            result.fill(0)
        }

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
        public actual abstract fun reset(newKey: ByteArray)

        private val code = Any()

        /** @suppress */
        public actual final override fun equals(other: Any?): Boolean = other is Engine && other.hashCode() == hashCode()
        /** @suppress */
        public actual final override fun hashCode(): Int = code.hashCode()
    }

    /** @suppress */
    public actual final override fun equals(other: Any?): Boolean = other is Mac && other.engine == engine
    /** @suppress */
    public actual final override fun hashCode(): Int = engine.hashCode()
    /** @suppress */
    public actual final override fun toString(): String = commonToString()
}
