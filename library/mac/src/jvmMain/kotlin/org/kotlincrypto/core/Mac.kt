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

import org.kotlincrypto.core.internal.AndroidApi23MacSpiProvider
import org.kotlincrypto.core.internal.commonInit
import org.kotlincrypto.core.internal.commonToString
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.Key
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.MacSpi
import javax.crypto.spec.SecretKeySpec

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
public actual abstract class Mac
@InternalKotlinCryptoApi
@Throws(IllegalArgumentException::class)
protected actual constructor(
    algorithm: String,
    private val engine: Engine,
) : javax.crypto.Mac(
    /* macSpi    */ engine,
    /* provider  */ AndroidApi23MacSpiProvider.createOrNull(engine, algorithm),
    /* algorithm */ algorithm
),  Algorithm,
    Copyable<Mac>,
    Resettable,
    Updatable
{

    init {
        commonInit(algorithm)

        // Engine.engineInit is overridden as no-op, so this does
        // nothing other than set `javax.crypto.Mac.initialized`
        // to true
        super.init(SecretKeySpec(ByteArray(1), algorithm))
    }

    public actual final override fun algorithm(): String = algorithm
    public actual fun macLength(): Int = macLength

    public actual final override fun copy(): Mac = copy(engine.copy())
    protected actual abstract fun copy(engineCopy: Engine): Mac

    public actual final override fun equals(other: Any?): Boolean = other is Mac && other.engine == engine
    public actual final override fun hashCode(): Int = engine.hashCode()
    public actual final override fun toString(): String = commonToString()

    /**
     * Core abstraction for powering a [Mac] implementation. Extends
     * Java's [MacSpi] for compatibility.
     *
     * Implementors of [Engine] **must** initialize the instance with the
     * provided key parameter.
     * */
    protected actual abstract class Engine: MacSpi, Cloneable, Copyable<Engine>, Resettable, Updatable {

        private val hashCode: Int = Any().hashCode()
        private var isInitialized = false

        /**
         * Initializes a new [Engine] with the provided [key].
         *
         * @throws [IllegalArgumentException] if [key] is empty.
         * */
        @InternalKotlinCryptoApi
        @Throws(IllegalArgumentException::class)
        public actual constructor(key: ByteArray): super() { require(key.isNotEmpty()) { "key cannot be empty" } }

        /**
         * Creates a new [Engine] for the copied [State]
         * */
        @InternalKotlinCryptoApi
        protected actual constructor(state: State): super()

        public actual abstract fun macLength(): Int
        public actual abstract fun doFinal(): ByteArray

        public actual override fun update(input: ByteArray) { update(input, 0, input.size) }

        protected final override fun engineUpdate(p0: Byte) { update(p0) }
        protected final override fun engineUpdate(input: ByteBuffer?) {
            if (input == null) return
            super.engineUpdate(input)
        }
        protected final override fun engineUpdate(p0: ByteArray, p1: Int, p2: Int) {
            // javax.crypto.Mac checks offset and len arguments
            update(p0, p1, p2)
        }
        protected final override fun engineReset() { reset() }
        protected final override fun engineGetMacLength(): Int = macLength()

        // Is called immediately from Mac init block with a blanked key (required in order to set
        // javax.crypto.Mac.initialized to true.
        protected final override fun engineInit(p0: Key?, p1: AlgorithmParameterSpec?) {
            if (isInitialized) {

                // Throw an exception b/c if caller is trying to re-init the javax.crypto.Mac with a new key,
                // the normal behavior is to blank the Spi state. If they do not know this is an issue,
                // any output would not be correct b/c implementations do not re-init. KotlinCrypto users
                // already know it's initialized because the API is designed to require the key upon instantiation
                // so init is never needed to be called.
                throw InvalidKeyException(
                    "org.kotlincrypto.Mac does not support re-initialization " +
                    "(it's already initialized). A new instance is required to be created."
                )
            }

            isInitialized = true
        }
        protected final override fun engineDoFinal(): ByteArray = doFinal()

        public final override fun clone(): Any = copy()

        public actual final override fun equals(other: Any?): Boolean = other is Engine && other.hashCode == hashCode
        public actual final override fun hashCode(): Int = hashCode

        protected actual abstract inner class State
    }
}
