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
package org.kotlincrypto.core.internal

import org.kotlincrypto.core.Copyable
import org.kotlincrypto.core.Resettable
import org.kotlincrypto.core.Updatable
import kotlin.jvm.JvmSynthetic

internal class DigestDelegate private constructor(
    internal val algorithm: String,
    internal val blockSize: Int,
    internal val digestLength: Int,
    private val buffer: ByteArray,
    private var bufferOffs: Int,
    private var compressCount: Long,
    private val compress: (buffer: ByteArray) -> Unit,
    private val digest: (bitLength: Long, bufferOffset: Int, buffer: ByteArray) -> ByteArray,
    private val resetDigest: () -> Unit
): Copyable<DigestState>, Resettable, Updatable {

    override fun update(input: Byte) {
        buffer[bufferOffs] = input

        if (++bufferOffs != blockSize) return
        compress(buffer)
        ++compressCount
        bufferOffs = 0
    }

    override fun update(input: ByteArray) {
        update(input, 0, input.size)
    }

    @Throws(IllegalArgumentException::class, IndexOutOfBoundsException::class)
    override fun update(input: ByteArray, offset: Int, len: Int) {
        if (input.size - offset < len) throw IllegalArgumentException("Input too short")
        if (len == 0) return
        if (offset < 0 || len < 0 || offset > input.size - len) throw IndexOutOfBoundsException()

        var i = offset
        var remaining = len - offset

        // fill buffer if not already empty
        while (bufferOffs != 0 && remaining > 0) {
            update(input[i++])
            --remaining
        }

        // chunk
        while (remaining >= blockSize) {
            input.copyInto(buffer, 0, i, i + blockSize)
            compress(buffer)
            i += blockSize
            remaining -= blockSize
            ++compressCount
        }

        // add remaining to buffer
        while (remaining-- > 0) {
            update(input[i++])
        }
    }

    internal fun digest(): ByteArray {
        val bitLength: Long = ((compressCount * blockSize) + bufferOffs) * 8
        val final = digest(bitLength, bufferOffs, buffer)
        reset()
        return final
    }

    override fun reset() {
        buffer.fill(0)
        bufferOffs = 0
        compressCount = 0
        resetDigest()
    }

    private inner class RealState: DigestState(algorithm, blockSize, digestLength) {
        val buffer = this@DigestDelegate.buffer.copyOf()
        val bufferOffs = this@DigestDelegate.bufferOffs
        val compressCount = this@DigestDelegate.compressCount
    }

    override fun copy(): DigestState = RealState()

    internal companion object {

        @JvmSynthetic
        internal fun instance(
            state: DigestState,
            compress: (buffer: ByteArray) -> Unit,
            digest: (bitLength: Long, bufferOffset: Int, buffer: ByteArray) -> ByteArray,
            resetDigest: () -> Unit
        ): DigestDelegate {
            return DigestDelegate(
                algorithm = (state as RealState).algorithm,
                blockSize = state.blockSize,
                digestLength = state.digestLength,
                buffer = state.buffer.copyOf(),
                bufferOffs = state.bufferOffs,
                compressCount = state.compressCount,
                compress = compress,
                digest = digest,
                resetDigest = resetDigest,
            )
        }

        @JvmSynthetic
        @Throws(IllegalArgumentException::class)
        internal fun instance(
            algorithm: String,
            blockSize: Int,
            digestLength: Int,
            compress: (buffer: ByteArray) -> Unit,
            digest: (bitLength: Long, bufferOffset: Int, buffer: ByteArray) -> ByteArray,
            resetDigest: () -> Unit
        ): DigestDelegate {

            require(algorithm.isNotBlank()) { "algorithm cannot be blank" }
            require(blockSize > 0) { "blockSize must be greater than 0" }
            require(blockSize % 8 == 0) { "blockSize must be a factor of 8" }
            require(digestLength > 0) { "digestLength must be greater than 0" }

            return DigestDelegate(
                algorithm = algorithm,
                blockSize = blockSize,
                digestLength = digestLength,
                buffer = ByteArray(blockSize),
                bufferOffs = 0,
                compressCount = 0L,
                compress = compress,
                digest = digest,
                resetDigest = resetDigest,
            )
        }
    }
}
