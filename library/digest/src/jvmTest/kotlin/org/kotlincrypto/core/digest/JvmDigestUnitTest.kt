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
package org.kotlincrypto.core.digest

import java.lang.AssertionError
import java.security.DigestException
import java.security.MessageDigest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Compares [Digest] functionality to [MessageDigest]
 * */
@Suppress("DEPRECATION")
class JvmDigestUnitTest {

    class MessageDigestWrap: Digest {

        private val delegate: MessageDigest

        constructor(digest: MessageDigest, blockSize: Int): super(digest.algorithm, blockSize, digest.digestLength) {
            delegate = digest.clone() as MessageDigest
        }
        private constructor(other: MessageDigestWrap): super(other) {
            delegate = other.delegate.clone() as MessageDigest
        }

        override fun copy(): MessageDigestWrap = MessageDigestWrap(this)

        override fun compressProtected(input: ByteArray, offset: Int) {
            delegate.update(input, offset, blockSize())
        }

        override fun digestProtected(buf: ByteArray, bufPos: Int): ByteArray {
            delegate.update(buf, 0, bufPos)
            return delegate.digest()
        }

        override fun resetProtected() {
            delegate.reset()
        }
    }

    private val jvm = MessageDigest.getInstance("SHA-256")
    private val wrap = MessageDigestWrap(jvm, 64)
    private val bytes = Random.Default.nextBytes(1_000)

    @Test(AssertionError::class)
    fun givenWrappedMessageDigest_whenOneUpdated_thenReturnsDifferent() {
        // To ensure that the wrapped digest test class
        // actually cloned the digest
        wrap.update(bytes)
        assertContentEquals(jvm.digest(), wrap.digest())
    }

    @Test
    fun givenWrappedMessageDigest_whenUpdated_thenReturnsTheSame() {
        wrap.update(bytes)
        jvm.update(bytes)

        assertContentEquals(jvm.digest(), wrap.digest())
    }

    @Test
    fun givenWrappedMessageDigest_whenUpdatedByte_thenReturnsTheSame() {
        wrap.update(bytes[0])
        jvm.update(bytes[0])

        assertContentEquals(jvm.digest(), wrap.digest())
    }

    @Test
    fun givenWrappedMessageDigest_whenUpdatedOffset_thenReturnsTheSame() {
        wrap.update(bytes, 10, 100)
        jvm.update(bytes, 10, 100)

        assertContentEquals(jvm.digest(), wrap.digest())

        wrap.update(bytes, 500, 1)
        jvm.update(bytes, 500, 1)

        assertContentEquals(jvm.digest(), wrap.digest())
    }

    @Test
    fun givenWrappedMessageDigest_whenDigestToBuf_thenWorksAsExpected() {
        wrap.update(bytes, 10, 100)
        jvm.update(bytes, 10, 100)
        val expected = wrap.digest()
        wrap.update(bytes, 10, 100)

        assertFailsWith<IllegalArgumentException> { wrap.digest(ByteArray(2), 0, wrap.digestLength()) }
        assertFailsWith<IllegalArgumentException> { jvm.digest(ByteArray(2), 0, wrap.digestLength()) }

        assertFailsWith<IllegalArgumentException> { wrap.digest(ByteArray(wrap.digestLength()), 1, wrap.digestLength()) }
        assertFailsWith<IllegalArgumentException> { jvm.digest(ByteArray(wrap.digestLength()), 1, wrap.digestLength()) }

        assertFailsWith<DigestException> { wrap.digest(ByteArray(wrap.digestLength()), -1, wrap.digestLength()) }
        assertFailsWith<DigestException> { jvm.digest(ByteArray(wrap.digestLength()), -1, wrap.digestLength()) }

        assertFailsWith<DigestException> { wrap.digest(ByteArray(wrap.digestLength()), 0, 0) }
        assertFailsWith<DigestException> { jvm.digest(ByteArray(wrap.digestLength()), 0, 0) }

        assertFailsWith<IllegalArgumentException> { wrap.digest(ByteArray(wrap.digestLength()), wrap.digestLength() + 1, wrap.digestLength()) }
        assertFailsWith<IllegalArgumentException> { jvm.digest(ByteArray(wrap.digestLength()), wrap.digestLength() + 1, wrap.digestLength()) }

        assertFailsWith<IllegalArgumentException> { wrap.digest(null, 0, wrap.digestLength()) }
        assertFailsWith<IllegalArgumentException> { jvm.digest(null, 0, wrap.digestLength()) }

        assertContentEquals(expected, wrap.digest())
        assertContentEquals(expected, jvm.digest())

        wrap.update(bytes, 10, 100)
        jvm.update(bytes, 10, 100)

        assertFailsWith<DigestException> { wrap.digest(ByteArray(wrap.digestLength()), 0, wrap.digestLength() - 1) }
        assertFailsWith<DigestException> { jvm.digest(ByteArray(wrap.digestLength()), 0, wrap.digestLength() - 1) }

        assertContentEquals(expected, wrap.digest())
        assertContentEquals(expected, jvm.digest())

        wrap.update(bytes, 10, 100)
        jvm.update(bytes, 10, 100)

        val resultWrap = ByteArray(wrap.digestLength() + 4)
        val resultJvm = resultWrap.copyOf()

        // Expressing longer length than what digest outputs should be ignored
        assertEquals(wrap.digestLength(), wrap.digest(resultWrap, 2, resultWrap.size - 2))
        assertEquals(wrap.digestLength(), jvm.digest(resultJvm, 2, resultJvm.size - 2))

        assertContentEquals(resultWrap, resultJvm)
        for (i in expected.indices) {
            assertEquals(expected[i], resultWrap[i + 2])
            assertEquals(expected[i], resultJvm[i + 2])
        }
        for (i in 0 until 2) {
            assertEquals(0, resultWrap[i])
            assertEquals(0, resultJvm[i])
        }
        for (i in (resultWrap.size - 2) until resultWrap.size) {
            assertEquals(0, resultWrap[i])
            assertEquals(0, resultJvm[i])
        }
    }
}
