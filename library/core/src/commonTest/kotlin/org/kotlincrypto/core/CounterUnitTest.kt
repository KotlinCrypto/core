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
import kotlin.test.assertNotEquals

class CounterUnitTest {

    @Test
    fun givenIncrementBy_when0_thenThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            Counter.Bit32(0)
        }
        assertFailsWith<IllegalArgumentException> {
            Counter.Bit64(0)
        }
    }

    @Test
    fun givenIncrementBy_whenExceedsMaximum_thenThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            Counter.Bit32(Counter.Bit32.MAX_INCREMENT + 8)
        }
        assertFailsWith<IllegalArgumentException> {
            Counter.Bit64(Counter.Bit64.MAX_INCREMENT + 8)
        }
    }

    @Test
    fun givenIncrementBy_whenNotFactory8_thenThrowsException() {
        assertFailsWith<IllegalArgumentException> {
            Counter.Bit32(9)
        }
        assertFailsWith<IllegalArgumentException> {
            Counter.Bit64(9)
        }
    }

    @Test
    fun givenLo_whenNotFactoryIncrementBy_thenThrowsException() {
        // Would throw if 24 was not a factor of 8...
        Counter.Bit32(24)
        Counter.Bit64(24)

        assertFailsWith<IllegalArgumentException> {
            Counter.Bit32(8, 0, 24)
        }
        assertFailsWith<IllegalArgumentException> {
            Counter.Bit64(16, 0, 24)
        }
    }

    @Test
    fun givenBit32_whenReset_thenIsZero() {
        val c = Counter.Bit32(8, 8, 8)
        assertEquals(8, c.lo)
        assertEquals(8, c.hi)
        assertEquals(8, c.incrementBy)
        c.reset()
        assertEquals(0, c.lo)
        assertEquals(0, c.hi)
    }

    @Test
    fun givenBit64_whenReset_thenIsZero() {
        val c = Counter.Bit64(8, 8, 8)
        assertEquals(8, c.lo)
        assertEquals(8, c.hi)
        assertEquals(8, c.incrementBy)
        c.reset()
        assertEquals(0, c.lo)
        assertEquals(0, c.hi)
    }

    @Test
    fun givenBit32_whenIncrement_thenIncrements() {
        var c = Counter.Bit32(-16, 0, 8)
        c.increment()
        assertEquals(-8, c.lo)
        assertEquals(0, c.hi)
        c.increment()
        assertEquals(0, c.lo)
        assertEquals(1, c.hi)

        c = Counter.Bit32(Int.MAX_VALUE - 7, 0, 8)
        c.increment()
        assertEquals(Int.MIN_VALUE, c.lo)
        assertEquals(0, c.hi)
    }

    @Test
    fun givenBit64_whenIncrement_thenIncrements() {
        var c = Counter.Bit64(-16, 0, 8)
        c.increment()
        assertEquals(-8, c.lo)
        assertEquals(0, c.hi)
        c.increment()
        assertEquals(0, c.lo)
        assertEquals(1, c.hi)

        c = Counter.Bit64(Long.MAX_VALUE - 7, 0, 8)
        c.increment()
        assertEquals(Long.MIN_VALUE, c.lo)
        assertEquals(0, c.hi)
    }

    @Test
    fun givenBit32_whenCopy_thenCopiesValues() {
        val expected = Counter.Bit32(8, 8, 8)
        val copy = expected.copy()
        assertNotEquals(expected, copy)
        assertEquals(expected.incrementBy, copy.incrementBy)
        assertEquals(expected.lo, copy.lo)
        assertEquals(expected.hi, copy.hi)
        copy.increment()
        assertNotEquals(expected.lo, copy.lo)
    }

    @Test
    fun givenBit64_whenCopy_thenCopiesValues() {
        val expected = Counter.Bit64(8, 8, 8)
        val copy = expected.copy()
        assertNotEquals(expected, copy)
        assertEquals(expected.incrementBy, copy.incrementBy)
        assertEquals(expected.lo, copy.lo)
        assertEquals(expected.hi, copy.hi)
        copy.increment()
        assertNotEquals(expected.lo, copy.lo)
    }

    @Test
    fun givenBit32_whenFinal_thenAdditionalValueIsAsExpected() {
        val counter = Counter.Bit32(-8, 0, 8)

        val (lo1, hi1) = counter.final(additional = 9)
        assertEquals(1, lo1)
        assertEquals(1, hi1)

        val (lo2, hi2) = counter.final(additional = 8)
        assertEquals(0, lo2)
        assertEquals(1, hi2)

        val (lo3, hi3) = counter.final(additional = 7)
        assertEquals(-1, lo3)
        assertEquals(0, hi3)
    }

    @Test
    fun givenBit64_whenFinal_thenAdditionalValueIsAsExpected() {
        val counter = Counter.Bit64(-8, 0, 8)

        val (lo1, hi1) = counter.final(additional = 9)
        assertEquals(1, lo1)
        assertEquals(1, hi1)

        val (lo2, hi2) = counter.final(additional = 8)
        assertEquals(0, lo2)
        assertEquals(1, hi2)

        val (lo3, hi3) = counter.final(additional = 7)
        assertEquals(-1, lo3)
        assertEquals(0, hi3)
    }

    @Test
    fun givenBit32Final_whenAsBits_thenConvertsAsExpected() {
        val number = 5551889119L
        val final = Counter.Bit32.Final(lo = number.toInt(), hi = number.rotateLeft(32).toInt())
        val bits = final.asBits()
        assertNotEquals(final, bits)

        // Should return same instance (already bits)
        assertEquals(bits, bits.asBits())

        val expected = number * Byte.SIZE_BITS
        val actual = ((bits.hi.toLong() and 0xffffffff) shl 32) or (bits.lo.toLong() and 0xffffffff)
        assertEquals(expected, actual)
    }

    @Test
    fun givenBit64Final_whenAsBits_thenConvertsAsExpected() {
        val number = 5551889119L
        val final = Counter.Bit64.Final(lo = number, hi = 1)
        val bits = final.asBits()
        assertNotEquals(final, bits)

        // Should return same instance (already bits)
        assertEquals(bits, bits.asBits())

        var expected = Long.MAX_VALUE
        expected += number + 1
        expected *= Byte.SIZE_BITS
        val actual = ((bits.hi and 0xffffffff) shl 32) or (bits.lo and 0xffffffff)
        assertEquals(expected, actual)
    }
}
