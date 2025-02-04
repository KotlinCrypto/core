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

import org.kotlincrypto.core.ShortBufferException
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
        val finalExpected = ByteArray(20) { it.toByte() }

        val mac = TestMac(
            ByteArray(5),
            "not blank",
            macLen = finalExpected.size,
            reset = { resetCount++ },
            doFinal = { doFinalCount++; finalExpected },
        )
        mac.update(ByteArray(20))

        // doFinal
        assertEquals(finalExpected, mac.doFinal())
        assertEquals(1, doFinalCount)
        assertEquals(1, resetCount)

        // update & doFinal
        assertEquals(finalExpected, mac.doFinal(ByteArray(20)))
        assertEquals(2, doFinalCount)
        assertEquals(2, resetCount)

        // doFinalInto
        assertEquals(finalExpected.size, mac.doFinalInto(ByteArray(25), 0))
        assertEquals(3, doFinalCount)
        assertEquals(3, resetCount)
    }

    @Test
    fun givenMacEngine_whenResetOnDoFinalFalse_thenEngineResetIsNOTCalled() {
        var resetCount = 0
        var doFinalCount = 0
        val finalExpected = ByteArray(15) { (it + 25).toByte() }

        val mac = TestMac(
            ByteArray(5),
            algorithm = "test resetOnDoFinal false",
            macLen = finalExpected.size,
            resetOnDoFinal = false,
            reset = { resetCount++ },
            doFinal = { doFinalCount++; finalExpected },
        )

        mac.reset()
        assertEquals(1, resetCount)

        // doFinal
        mac.doFinal()
        assertEquals(1, doFinalCount)
        assertEquals(1, resetCount)

        // update & doFinal
        mac.doFinal(ByteArray(25))
        assertEquals(2, doFinalCount)
        assertEquals(1, resetCount)

        // doFinalInto
        mac.doFinalInto(ByteArray(finalExpected.size), 0)
        assertEquals(3, doFinalCount)
        assertEquals(1, resetCount)

        mac.reset()
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

    @Test
    fun givenMac_whenDoFinalInto_thenDefaultImplementationCopiesResultIntoDest() {
        val expected = ByteArray(10) { 1 }
        val mac = TestMac(
            key = ByteArray(5),
            algorithm = "test doFinalInto",
            macLen = expected.size,
            doFinal = { expected.copyOf() }
        )
        val actual = ByteArray(expected.size + 2) { 4 }
        mac.doFinalInto(actual, 1)

        assertEquals(4, actual[0])
        assertEquals(4, actual[actual.size - 1])
        for (i in expected.indices) {
            assertEquals(expected[i], actual[i + 1])
        }
    }

    @Test
    fun givenMac_whenLength0_thenDoFinalIntoDoesNotFail() {
        val mac = TestMac(
            key = ByteArray(5),
            algorithm = "test doFinalInto 0",
            macLen = 0
        )

        mac.doFinalInto(ByteArray(0), 0)
        mac.doFinalInto(ByteArray(2), 1)
        mac.doFinalInto(ByteArray(2), 2)
    }

    @Test
    fun givenMac_whenDoFinalInto_thenThrowsExceptionsAsExpected() {
        val mac = TestMac(
            key = ByteArray(5),
            algorithm = "test doFinalInto Exceptions",
            macLen = 5,
        )
        val mSize = mac.macLength()
        assertFailsWith<ShortBufferException> { mac.doFinalInto(ByteArray(mSize), 1) }
        assertFailsWith<IndexOutOfBoundsException> { mac.doFinalInto(ByteArray(mSize), -1) }
    }
}
