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
import java.security.MessageDigest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals

/**
 * Compares [Digest] functionality to [MessageDigest]
 * */
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

        override fun digestProtected(buffer: ByteArray, offset: Int): ByteArray {
            delegate.update(buffer, 0, offset)
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
}
