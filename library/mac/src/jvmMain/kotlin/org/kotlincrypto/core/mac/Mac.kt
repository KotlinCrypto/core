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
import org.kotlincrypto.core.mac.internal.AndroidApi21to23MacSpiProvider
import org.kotlincrypto.core.mac.internal.commonInit
import org.kotlincrypto.core.mac.internal.commonToString
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.KeySpec
import javax.crypto.MacSpi
import javax.crypto.SecretKey

/**
 * Core abstraction for Message Authentication Code implementations. Extends
 * Java's [javax.crypto.Mac] for compatibility.
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
public actual abstract class Mac: javax.crypto.Mac, Algorithm, Copyable<Mac>, Resettable, Updatable {

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
    protected actual constructor(algorithm: String, engine: Engine): super(
        /* macSpi    */ engine,
        /* provider  */ AndroidApi21to23MacSpiProvider.createOrNull(engine, algorithm),
        /* algorithm */ algorithm
    ) {
        commonInit(algorithm)
        this.engine = engine

        // Engine.engineInit is overridden as no-op, so this does
        // nothing other than set `javax.crypto.Mac.initialized`
        // to true
        super.init(EmptyKey)
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
    public actual fun macLength(): Int = macLength

    // See Algorithm interface documentation
    public actual final override fun algorithm(): String = algorithm

    /** @suppress */
    public actual final override fun equals(other: Any?): Boolean = other is Mac && other.engine == engine
    /** @suppress */
    public actual final override fun hashCode(): Int = engine.hashCode()
    /** @suppress */
    public actual final override fun toString(): String = commonToString()

    /**
     * Core abstraction for powering a [Mac] implementation. Extends
     * Java's [MacSpi] for compatibility.
     *
     * Implementors of [Engine] **must** initialize the instance with the
     * provided key parameter.
     * */
    protected actual abstract class Engine: MacSpi, Cloneable, Copyable<Engine>, Resettable, Updatable {

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

        // MacSpi
        /** @suppress */
        protected final override fun engineUpdate(p0: Byte) { update(p0) }
        /** @suppress */
        protected final override fun engineUpdate(input: ByteBuffer?) {
            if (input == null) return
            super.engineUpdate(input)
        }
        /** @suppress */
        protected final override fun engineUpdate(p0: ByteArray, p1: Int, p2: Int) {
            // javax.crypto.Mac checks offset and len arguments
            update(p0, p1, p2)
        }
        /** @suppress */
        protected final override fun engineReset() { reset() }
        /** @suppress */
        protected final override fun engineGetMacLength(): Int = macLength()

        // Is called immediately from Mac init block with a blanked key (required in order to set
        // javax.crypto.Mac.initialized to true).
        /** @suppress */
        protected final override fun engineInit(p0: Key?, p1: AlgorithmParameterSpec?) {
            if (p0 is EmptyKey) return

            // Throw an exception b/c if caller is trying to re-init the javax.crypto.Mac with a new key,
            // the normal behavior is to blank the MacSpi state. If caller does not know this is an issue,
            // any further output would not be correct b/c implementations do not re-init. KotlinCrypto users
            // already know it's initialized because the API is designed to require the key upon instantiation
            // so init is never needed to be called, nor is init function available from commonMain source set.
            throw InvalidKeyException(
                "org.kotlincrypto.core.mac.Mac does not support re-initialization " +
                "(it's already initialized). A new instance is required to be created."
            )
        }
        /** @suppress */
        protected final override fun engineDoFinal(): ByteArray {
            val b = doFinal()

            // Android API 23 and below javax.crypto.Mac does not call engineReset()
            @OptIn(InternalKotlinCryptoApi::class)
            KC_ANDROID_SDK_INT?.let { if (it <= 23) reset() }

            return b
        }

        private val code = Any()

        /** @suppress */
        public final override fun clone(): Any = copy()
        /** @suppress */
        public actual final override fun equals(other: Any?): Boolean = other is Engine && other.hashCode() == hashCode()
        /** @suppress */
        public actual final override fun hashCode(): Int = 17 * 31 + code.hashCode()
    }

    private data object EmptyKey: KeySpec, SecretKey {
        override fun getAlgorithm(): String = "org.kotlincrypto.core.mac.Mac.EmptyKey"
        override fun getEncoded(): ByteArray? = null
        override fun getFormat(): String = "RAW"
        private fun readResolve(): Any = EmptyKey
    }
}
