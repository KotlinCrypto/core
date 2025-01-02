/*
 * Copyright (c) 2025 Matthew Nelson
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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CounterUnitTest {

    @Test
    fun givenIncrementBy_when0_thenThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            TestBit32Counter(0)
        }
        assertFailsWith<IllegalArgumentException> {
            TestBit64Counter(0)
        }
    }

    @Test
    fun givenIncrementBy_whenExceedsMaximum_thenThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            TestBit32Counter(Counter.Bit32.MAX_INCREMENT + 8)
        }
        assertFailsWith<IllegalArgumentException> {
            TestBit64Counter(Counter.Bit64.MAX_INCREMENT + 8)
        }
    }

    @Test
    fun givenIncrementBy_whenNotFactory8_thenThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            TestBit32Counter(9)
        }
        assertFailsWith<IllegalArgumentException> {
            TestBit64Counter(9)
        }
    }

    @Test
    fun givenLo_whenNotFactoryIncrementBy_thenThrowsException() {
        // Would throw if 24 was not a factor of 8...
        TestBit32Counter(24)
        TestBit64Counter(24)

        assertFailsWith<IllegalArgumentException> {
            TestBit32Counter(8, 0, 24)
        }
        assertFailsWith<IllegalArgumentException> {
            TestBit64Counter(16, 0, 24)
        }
    }

    @Test
    fun givenBit32_whenReset_thenIsZero() {
        val c = TestBit32Counter(8, 8, 8)
        assertEquals(8, c.lo)
        assertEquals(8, c.hi)
        assertEquals(8, c.incrementBy)
        c.reset()
        assertEquals(0, c.lo)
        assertEquals(0, c.hi)
    }

    @Test
    fun givenBit64_whenReset_thenIsZero() {
        val c = TestBit64Counter(8, 8, 8)
        assertEquals(8, c.lo)
        assertEquals(8, c.hi)
        assertEquals(8, c.incrementBy)
        c.reset()
        assertEquals(0, c.lo)
        assertEquals(0, c.hi)
    }

    @Test
    fun givenBit32_whenIncrement_thenIncrements() {
        var c = TestBit32Counter(-16, 0, 8)
        c.increment()
        assertEquals(-8, c.lo)
        assertEquals(0, c.hi)
        c.increment()
        assertEquals(0, c.lo)
        assertEquals(1, c.hi)

        c = TestBit32Counter(Int.MAX_VALUE - 7, 0, 8)
        c.increment()
        assertEquals(Int.MIN_VALUE, c.lo)
        assertEquals(0, c.hi)
    }

    @Test
    fun givenBit64_whenIncrement_thenIncrements() {
        var c = TestBit64Counter(-16, 0, 8)
        c.increment()
        assertEquals(-8, c.lo)
        assertEquals(0, c.hi)
        c.increment()
        assertEquals(0, c.lo)
        assertEquals(1, c.hi)

        c = TestBit64Counter(Long.MAX_VALUE - 7, 0, 8)
        c.increment()
        assertEquals(Long.MIN_VALUE, c.lo)
        assertEquals(0, c.hi)
    }

    @Test
    fun givenBit32_whenClone_thenCopiesValues() {
        val expected = TestBit32Counter(8, 8, 8)
        val actual = TestBit32Counter(expected)
        assertEquals(expected.incrementBy, actual.incrementBy)
        assertEquals(expected.lo, actual.lo)
        assertEquals(expected.hi, actual.hi)
    }
}
