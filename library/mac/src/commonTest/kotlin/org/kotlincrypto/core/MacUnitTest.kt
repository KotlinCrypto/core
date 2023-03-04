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

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.fail

class MacUnitTest {

    private class TestMac(key: ByteArray, algorithm: String) : Mac(algorithm, TestEngine(key)) {
        private class TestEngine: Engine {

            constructor(key: ByteArray): super(key)
            private constructor(state: State): super(state)

            // To ensure that Java implementation initializes javax.crypto.Mac
            // on instantiation so that it does not throw IllegalStateException
            // whenever updating.
            override fun update(input: Byte) { throw ConcurrentModificationException() }

            override fun reset() {}
            override fun update(input: ByteArray) {}
            override fun update(input: ByteArray, offset: Int, len: Int) {}
            override fun macLength(): Int = 0
            override fun doFinal(): ByteArray = ByteArray(0)

            override fun copy(): Engine = TestEngine(TestState())

            private inner class TestState: Engine.State()
        }
    }

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
}
