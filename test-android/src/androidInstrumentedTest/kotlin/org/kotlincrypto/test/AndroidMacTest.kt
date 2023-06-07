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
@file:Suppress("UnnecessaryOptInAnnotation")

package org.kotlincrypto.test

import org.kotlincrypto.core.InternalKotlinCryptoApi
import org.kotlincrypto.core.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test

class AndroidMacTest {

    @OptIn(InternalKotlinCryptoApi::class)
    class TestMac: Mac {

        constructor(key: ByteArray): this(Engine("HmacSHA256", key))

        private constructor(engine: Engine): super(engine.algorithm, engine)

        private class Engine: Mac.Engine {

            val algorithm: String
            val delegate: javax.crypto.Mac

            constructor(algorithm: String, key: ByteArray): super(key) {
                this.algorithm = algorithm
                this.delegate = getInstance(algorithm)
                this.delegate.init(SecretKeySpec(key, "HmacSHA256"))
            }

            private constructor(state: State, engine: Engine): super(state) {
                this.algorithm = engine.algorithm
                this.delegate = engine.delegate.clone() as javax.crypto.Mac
            }

            override fun update(input: Byte) {
                delegate.update(input)
            }

            override fun update(input: ByteArray, offset: Int, len: Int) {
                delegate.update(input, offset, len)
            }

            override fun doFinal(): ByteArray = delegate.doFinal()
            override fun macLength(): Int = delegate.macLength
            override fun copy(): Mac.Engine = Engine(object : State() {}, this)
            override fun reset() { delegate.reset() }
        }

        override fun copy(engineCopy: Mac.Engine): Mac = TestMac(engineCopy as Engine)
    }

    @Test
    fun givenAndroid_whenMacInstantiated_thenPasses() {
        // https://github.com/KotlinCrypto/core/issues/37
        val key = ByteArray(50) { it.toByte() }
        TestMac(key).doFinal()
    }
}
