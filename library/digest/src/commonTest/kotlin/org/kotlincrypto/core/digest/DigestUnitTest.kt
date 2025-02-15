/*
 * Copyright (c) 2023 KotlinCrypto
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
package org.kotlincrypto.core.digest

import org.kotlincrypto.error.InvalidParameterException
import org.kotlincrypto.error.ShortBufferException
import kotlin.random.Random
import kotlin.test.*

class DigestUnitTest: AbstractTestUpdateExceptions() {

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
    fun givenDigest_whenLengthNegative_thenThrowsException() {
        // accepts 0 length
        TestDigest(digestLength = 0)
        assertFailsWith<InvalidParameterException> { TestDigest(digestLength = -1) }
    }

    @Test
    fun givenDigest_whenDigested_thenResetIsInvoked() {
        val expected = ByteArray(10) { 1 }
        var resetCount = 0

        val digest = TestDigest(
            digest = { _, _ -> expected },
            reset = { resetCount++ }
        )

        assertEquals(expected, digest.digest())
        assertEquals(1, resetCount)
        assertEquals(expected, digest.digest())
        assertEquals(2, resetCount)
    }

    @Test
    fun givenDigest_whenUpdated_thenChunksProperly() {
        val digest = TestDigest(
            // Return byte array sized to the offset
            digest = { _, offset -> ByteArray(offset) }
        )

        digest.update(ByteArray(digest.blockSize() - 1))
        assertEquals(0, digest.compressions)

        digest.update(ByteArray(digest.blockSize() + 1))
        assertEquals(2, digest.compressions)

        digest.update(ByteArray(digest.blockSize() - 1))
        assertEquals(2, digest.compressions)

        digest.update(4)
        assertEquals(3, digest.compressions)

        digest.update(ByteArray(digest.blockSize()))
        assertEquals(4, digest.compressions)

        // Check the internal bufferOffset was 0 after all that
        assertEquals(0, digest.digest().size)
        assertEquals(0, digest.compressions)
    }

    @Test
    fun givenDigest_whenCopied_thenIsNewInstance() {
        val digest = TestDigest(
            digest = { b, _ ->
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

    @Test
    fun givenBuffer_whenDigestProtected_thenStaleInputIsZeroedOut() {
        var bufCopy: ByteArray? = null
        var bufCopyPos: Int = -1
        val digest = TestDigest(digest = { buf, bufPos ->
            bufCopy = buf.copyOf()
            bufCopyPos = bufPos
            buf
        })
        digest.update(Random.Default.nextBytes(digest.blockSize() - 1))
        assertEquals(0, digest.compressions)
        digest.update(5)
        assertEquals(1, digest.compressions)

        val expected: Byte = -42
        digest.update(ByteArray(digest.blockSize() - 10) { expected })
        assertEquals(1, digest.compressions)
        digest.digest()

        assertNotNull(bufCopy)
        assertNotEquals(-1, bufCopyPos)
        assertNotEquals(0, bufCopyPos)
        assertNotEquals(digest.blockSize(), bufCopyPos)

        for (i in 0 until bufCopyPos) {
            assertEquals(expected, bufCopy!![i])
        }

        for (i in bufCopyPos until digest.blockSize()) {
            assertEquals(0, bufCopy!![i])
        }
    }

    @Test
    fun givenDigest_whenDigestInto_thenDefaultImplementationCopiesResultIntoDest() {
        val expected = ByteArray(10) { 1 }
        val digest = TestDigest(
            digestLength = expected.size,
            digest = { _, _ -> expected.copyOf() }
        )
        val actual = ByteArray(expected.size + 2) { 4 }
        digest.digestInto(actual, 1)

        assertEquals(4, actual[0])
        assertEquals(4, actual[actual.size - 1])
        for (i in expected.indices) {
            assertEquals(expected[i], actual[i + 1])
        }
    }

    @Test
    fun givenDigest_whenDigestInto_thenThrowsExceptionsAsExpected() {
        val dSize = digest.digestLength()
        assertFailsWith<ShortBufferException> { digest.digestInto(ByteArray(dSize), 1) }
        assertFailsWith<IndexOutOfBoundsException> { digest.digestInto(ByteArray(dSize), -1) }
    }
}
