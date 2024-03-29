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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.fail

class MacUnitTest {

    @Test
    fun givenMac_whenEmptyKey_thenThrowsException() {
        try {
            TestMac(ByteArray(0), "not empty")
            fail()
        } catch (_: IllegalArgumentException) {
            // pass
        }
    }

    @Test
    fun givenMac_whenBlankAlgorithm_thenThrowsException() {
        try {
            TestMac(ByteArray(5), "  ")
            fail()
        } catch (_: IllegalArgumentException) {
            // pass
        }
    }

    @Test
    fun givenMac_whenInstantiated_thenInitializes() {
        try {
            TestMac(ByteArray(5), "not blank").update(5)
            fail()
        } catch (_: ConcurrentModificationException) {
            // pass
        }
    }

    @Test
    fun givenMac_whenCopied_thenIsNewInstance() {
        val mac = TestMac(ByteArray(5), "not blank")
        val copy = mac.copy()

        assertNotEquals(mac, copy)
    }

    @Test
    fun givenMac_whenDoFinal_thenEngineResetIsCalled() {
        var resetCount = 0
        var doFinalCount = 0
        val finalExpected = ByteArray(20)

        val mac = TestMac(
            ByteArray(5),
            "not blank",
            reset = { resetCount++ },
            doFinal = { doFinalCount++; finalExpected },
        )
        mac.update(ByteArray(20))
        assertEquals(finalExpected, mac.doFinal())
        assertEquals(1, doFinalCount)
        assertEquals(1, resetCount)

        assertEquals(finalExpected, mac.doFinal(ByteArray(20)))
        assertEquals(2, doFinalCount)
        assertEquals(2, resetCount)
    }
}
