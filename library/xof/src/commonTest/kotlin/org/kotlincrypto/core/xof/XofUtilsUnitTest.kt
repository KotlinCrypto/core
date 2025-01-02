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
package org.kotlincrypto.core.xof

import org.kotlincrypto.core.InternalKotlinCryptoApi
import kotlin.test.Test
import kotlin.test.assertContentEquals

@OptIn(InternalKotlinCryptoApi::class)
class XofUtilsUnitTest {

    @Test
    fun givenValue_whenEncoded_thenIsAsExpected() {
        listOf(
                   777711L to byteArrayOf(3, 11, -35, -17),
                  -777711L to byteArrayOf(8, -1, -1, -1, -1, -1, -12, 34, 17),
                      555L to byteArrayOf(2, 2, 43),
            Long.MIN_VALUE to byteArrayOf(8, -128, 0, 0, 0, 0, 0, 0, 0),
            Long.MAX_VALUE to byteArrayOf(8, 127, -1, -1, -1, -1, -1, -1, -1),
        ).forEach { (value, expected) ->
            assertContentEquals(expected, Xof.Utils.leftEncode(value))

            // Shift expected for right encoding
            var i = 0
            while (i < expected.lastIndex) {
                val old = expected[i]
                val new = expected[i + 1]
                expected[i] = new
                expected[i + 1] = old
                i++
            }

            assertContentEquals(expected, Xof.Utils.rightEncode(value))
        }
    }

    @Test
    fun givenLeftEncoding_whenValueZero_thenResultIsAsExpected() {
        val expected = ByteArray(2).apply { this[0] = 1 }
        assertContentEquals(expected, Xof.Utils.leftEncode(0L))
    }

    @Test
    fun givenRightEncoding_whenValueZero_thenResultIsAsExpected() {
        val expected = ByteArray(2).apply { this[1] = 1 }
        assertContentEquals(expected, Xof.Utils.rightEncode(0L))
    }
}
