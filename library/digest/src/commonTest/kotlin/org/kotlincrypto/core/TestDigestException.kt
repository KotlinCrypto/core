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
import kotlin.test.fail

/**
 * Ensures that all exception cases are handled the
 * same for [Digest] as Java's MessageDigest would handle
 * them.
 * */
abstract class TestDigestException: Updatable {

    @Test
    fun givenDigest_whenEmptyBytes_thenDoesNotThrow() {
        update(ByteArray(0))
    }

    @Test
    fun givenDigest_whenLength0_thenThrowsExpected() {
        // Input length check will pass here, but
        // this would produce an ArrayIndexOutOfBounds exception
        // unless len is 0
        update(ByteArray(0), -1, 0)

        // This ensures that both jvm and non-Jvm check
        // input length _before_ length == 0 which will
        // simply return and not produce an error.
        //
        // If len > 0, this would throw an ArrayIndexOutOfBounds
        // exception because offset > input.size
        try {
            update(ByteArray(0), 1, 0)
            fail()
        } catch (_: IllegalArgumentException) {
            // pass
        }
    }

    @Test
    fun givenDigest_whenLengthNegative_thenThrows() {
        try {
            update(ByteArray(0), 0, -1)
            fail()
        } catch (_: IndexOutOfBoundsException) {
            // pass
        }
    }

    @Test
    fun givenDigest_whenOffsetNegative_thenThrows() {
        try {
            update(ByteArray(10), -1, 10)
            fail()
        } catch (_: IndexOutOfBoundsException) {
            // pass
        }
    }

    @Test
    fun givenDigest_whenInputTooShort_thenThrows() {
        try {
            update(ByteArray(5), -1, 10)
            fail()
        } catch (_: IllegalArgumentException) {
            // pass
        }
    }
}
