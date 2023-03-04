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
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DigestUnitTest: TestDigestException() {

    private val digest = TestDigest()

    override fun update(input: ByteArray) {
        digest.update(input)
    }

    override fun update(input: Byte) {
        digest.update(input)
    }

    override fun update(input: ByteArray, offset: Int, len: Int) {
        digest.update(input, offset, len)
    }

    @Test
    fun givenDigest_whenDigested_thenResetIsInvoked() {
        val expected = ByteArray(10) { 1 }
        var resetCount = 0

        val digest = TestDigest(
            digest = { _, _, _ -> expected },
            reset = { resetCount++ }
        )

        assertEquals(expected, digest.digest())
        assertEquals(1, resetCount)
        assertEquals(expected, digest.digest())
        assertEquals(2, resetCount)
    }

    @Test
    fun givenDigest_whenUpdated_thenChunksProperly() {
        var compressCount = 0

        val digest = TestDigest(
            compress = { compressCount++ },
            // Return byte array sized to the offset
            digest = { _, offset, _ -> ByteArray(offset) }
        )

        digest.update(ByteArray(digest.blockSize() - 1))
        assertEquals(0, compressCount)

        digest.update(ByteArray(digest.blockSize() + 1))
        assertEquals(2, compressCount)

        digest.update(ByteArray(digest.blockSize() - 1))
        assertEquals(2, compressCount)

        digest.update(4)
        assertEquals(3, compressCount)

        digest.update(ByteArray(digest.blockSize()))
        assertEquals(4, compressCount)

        // Check the internal bufferOffset was 0 after all that
        assertEquals(0, digest.digest().size)
    }

    @Test
    fun givenDigest_whenCopied_thenIsNewInstance() {
        val digest = TestDigest(
            digest = { _, _, b ->
                assertEquals(1, b[0])
                assertEquals(0, b[1])
                b
            }
        ).apply {
            update(1)
        }

        val copy = digest.copy()

        assertEquals(digest.blockSize(), copy.blockSize())
        assertEquals(digest.digestLength(), copy.digestLength())
        assertEquals(digest.algorithm(), copy.algorithm())
        assertNotEquals(copy, digest)

        val digestDigest = digest.digest()
        val copyDigest = copy.digest()

        assertNotEquals(copyDigest, digestDigest)
        assertEquals(digestDigest.size, copyDigest.size)
    }
}
