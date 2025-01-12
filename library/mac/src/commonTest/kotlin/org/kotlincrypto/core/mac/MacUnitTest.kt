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

import kotlin.test.*

class MacUnitTest {

    @Test
    fun givenMac_whenEmptyKey_thenThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            TestMac(ByteArray(0), "not empty")
        }
    }

    @Test
    fun givenMac_whenBlankAlgorithm_thenThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            TestMac(ByteArray(5), "  ")
        }
    }

    @Test
    fun givenMac_whenResetWithEmptyKey_thenThrowsException() {
        val mac = TestMac(ByteArray(5), "my algorithm")
        assertFailsWith<IllegalArgumentException> {
            mac.reset(ByteArray(0))
        }
    }

    @Test
    fun givenMac_whenInstantiated_thenInitializes() {
        val mac = TestMac(ByteArray(5), "not blank")
        assertFailsWith<ConcurrentModificationException> {
            // test mac throws ConcurrentModificationException
            // on update for single byte input for verifying
            // instantiation is in working  order.
            mac.update(5)
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

    @Test
    fun givenMac_whenClearKey_thenSingle0ByteKeyPassedToEngine() {
        var zeroKey: ByteArray? = null

        TestMac(
            ByteArray(5),
            "test rekey",
            rekey = { zeroKey = it }
        ).clearKey()

        assertNotNull(zeroKey)
        assertContentEquals(ByteArray(1) { 0 }, zeroKey)
    }
}
