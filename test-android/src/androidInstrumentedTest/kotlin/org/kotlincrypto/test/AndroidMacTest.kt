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

import android.os.Build
import org.kotlincrypto.core.InternalKotlinCryptoApi
import org.kotlincrypto.core.Mac
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import javax.crypto.MacSpi
import javax.crypto.spec.SecretKeySpec
import kotlin.test.*

class AndroidMacTest {

    private val key = ByteArray(50) { it.toByte() }

    @OptIn(InternalKotlinCryptoApi::class)
    class TestMac: Mac {

        private val engine: Engine

        fun updateCount(): Int = engine.count

        constructor(key: ByteArray): this(Engine("Anything????", key))

        private constructor(engine: Engine): super(engine.algorithm, engine) {
            this.engine = engine
        }

        private class Engine: Mac.Engine {

            var count = 0
            val algorithm: String
            val delegate: javax.crypto.Mac

            constructor(algorithm: String, key: ByteArray): super(key) {
                this.algorithm = algorithm

                // Use HmacSHA256 for the tests such that we can get a non-static
                // result.
                this.delegate = getInstance("HmacSHA256")
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
                count++
            }

            override fun doFinal(): ByteArray = delegate.doFinal()
            override fun macLength(): Int = delegate.macLength
            override fun copy(): Mac.Engine = Engine(object : State() {}, this)
            override fun reset() { delegate.reset() }
        }

        override fun copy(engineCopy: Mac.Engine): Mac = TestMac(engineCopy as Engine)
    }

    @Test
    fun givenAndroid_whenMacInstantiated_thenUsesProvidedEngine() {
        val testMac = TestMac(key)
        testMac.apply { update(key) }.doFinal()

        assertEquals(1, testMac.updateCount())
    }

    @Test
    fun givenAndroid_whenApi23OrBelow_thenUsesProvider() {
        val provider = TestMac(key).provider
        if (Build.VERSION.SDK_INT < 24) {
            assertNotNull(provider)
        } else {
            assertNull(provider)
        }
    }

    @Test
    fun givenAndroid_whenProvider_spiIsDeReferencedAfterInitialRetrieval() {
        val mac = TestMac(key)
        val provider = mac.provider ?: return // no test

        val service = provider.getService("Mac", mac.algorithm)
        val instance = service.newInstance(null)
        assertFalse(instance is MacSpi)

        // should return the service instance
        assertEquals(service, instance)
    }

    @Test
    fun givenAndroid_whenProvider_thenIsNotCachedInMacSERVICE() {
        val mac = TestMac(key)
        val provider = mac.provider ?: return // no test

        // Android 23 and below javax.crypto.Mac uses
        // a static org.apache.harmony.security.fortress.Engine
        //
        // this simply ensures that our wrapper provider is not being
        // cached. This is the horrible provider architecture rearing
        // its head.

        try {
            javax.crypto.Mac.getInstance(mac.algorithm)
            fail()
        } catch (_: NoSuchAlgorithmException) {
            // pass
        }

        try {
            javax.crypto.Mac.getInstance(mac.algorithm, provider.name)
            fail()
        } catch (_: NoSuchProviderException) {
            // pass
        }

        try {
            // Even if the provider is used in getInstance, the spi should
            // be de-referenced which would return null and then throw
            // an exception here. We do NOT want any provider apis used
            // to obtain an instance of the spi.
            javax.crypto.Mac.getInstance(mac.algorithm, provider)
            fail()
        } catch (_: NoSuchAlgorithmException) {
            // pass
        }
    }
}
