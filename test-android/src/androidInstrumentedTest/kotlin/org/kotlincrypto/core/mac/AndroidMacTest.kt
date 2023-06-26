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

package org.kotlincrypto.core.mac

import android.os.Build
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.Provider
import kotlin.test.*

class AndroidMacTest {

    private val key = ByteArray(50) { it.toByte() }

    @Test
    fun givenAndroid_whenApi23OrBelow_thenUsesProvider() {
        val provider = TestMac(key, TEST_ALGORITHM).provider
        if (Build.VERSION.SDK_INT in 21..23) {
            assertNotNull(provider)
        } else {
            assertNull(provider)
        }
    }

    @Test
    fun givenAndroid_whenProvider_getServiceThrowsException() {
        val (mac, provider) = testMacAndProviderOrNull() ?: return

        try {
            provider.getService("Mac", mac.algorithm)
            fail()
        } catch (_: NoSuchAlgorithmException) {
            // pass
        }
    }

    @Test
    fun givenAndroid_whenMacGetInstanceForAlgorithm_thenIsNotCachedInMacSERVICE() {
        val (mac, _) = testMacAndProviderOrNull() ?: return

        try {
            javax.crypto.Mac.getInstance(mac.algorithm)
            fail()
        } catch (_: NoSuchAlgorithmException) {
            // pass
        }
    }

    @Test
    fun givenAndroid_whenMacGetInstanceForAlgorithmAndProviderName_thenIsNotCachedInMacSERVICE() {
        val (mac, provider) = testMacAndProviderOrNull() ?: return

        try {
            javax.crypto.Mac.getInstance(mac.algorithm, provider.name)
            fail()
        } catch (_: NoSuchProviderException) {
            // pass
        }
    }

    @Test
    fun givenAndroid_whenMacGetInstanceForAlgorithmAndProvider_thenIsNotCachedInMacSERVICE() {
        val (mac, provider) = testMacAndProviderOrNull() ?: return

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

    private fun testMacAndProviderOrNull(): Pair<Mac, Provider>? {
        val mac = TestMac(key, TEST_ALGORITHM)
        val provider = mac.provider ?: return null
        return Pair(mac, provider)
    }

    private companion object {
        private const val TEST_ALGORITHM = "AndroidMacTest"
    }
}
