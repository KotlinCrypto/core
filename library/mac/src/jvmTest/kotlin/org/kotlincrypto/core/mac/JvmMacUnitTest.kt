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
package org.kotlincrypto.core.mac

import junit.framework.TestCase.assertEquals
import org.kotlincrypto.core.mac.TestMac
import java.security.InvalidKeyException
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.fail

class JvmMacUnitTest {

    private val key = ByteArray(20) { it.toByte() }

    @Test
    fun givenJvm_whenNotAndroid_providerIsNotSet() {
        val mac = TestMac(key, "My Algorithm", doFinal = { key })
        assertEquals(key, mac.doFinal())
        assertNull(mac.provider)
    }

    @Test
    fun givenJvm_whenJavaxCryptoMacInitInvoked_thenThrowsException() {
        val mac = TestMac(key, "My Algorithm")
        val keySpec = SecretKeySpec(key, mac.algorithm())

        try {
            mac.init(keySpec)
            fail()
        } catch (_: InvalidKeyException) {
            // pass
        }

        try {
            mac.copy().init(keySpec)
            fail()
        } catch (_: InvalidKeyException) {
            // pass
        }
    }
}
