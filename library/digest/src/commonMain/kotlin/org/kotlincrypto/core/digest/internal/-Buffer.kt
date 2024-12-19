/*
 * Copyright (c) 2024 Matthew Nelson
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
@file:Suppress("KotlinRedundantDiagnosticSuppress")

package org.kotlincrypto.core.digest.internal

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmSynthetic

@JvmInline
internal value class Buffer private constructor(internal val value: ByteArray) {

    internal fun toState(
        algorithm: String,
        digestLength: Int,
        bufOffs: Int,
        compressCount: Long,
    ): DigestState = State(
        algorithm,
        digestLength,
        bufOffs,
        compressCount,
        this,
    )

    private class State(
        algorithm: String,
        digestLength: Int,
        bufOffs: Int,
        compressCount: Long,
        buf: Buffer,
    ): DigestState(
        algorithm,
        digestLength,
        bufOffs,
        compressCount,
    ) {
        val buf = Buffer(buf.value.copyOf())
    }

    internal companion object {

        @JvmSynthetic
        internal fun DigestState.buf(): Buffer = Buffer((this as State).buf.value.copyOf())

        @JvmSynthetic
        @Throws(IllegalArgumentException::class)
        internal fun initialize(
            algorithm: String,
            blockSize: Int,
            digestLength: Int,
        ): Buffer {
            require(algorithm.isNotBlank()) { "algorithm cannot be blank" }
            require(blockSize > 0) { "blockSize must be greater than 0" }
            require(blockSize % 8 == 0) { "blockSize must be a factor of 8" }
            require(digestLength >= 0) { "digestLength cannot be negative" }
            return Buffer(ByteArray(blockSize))
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Buffer.update(
    input: Byte,
    bufOffsGetAndIncrement: () -> Int,
    bufOffsReset: () -> Unit,
    compress: (buf: ByteArray, offset: Int) -> Unit,
    compressCountIncrement: () -> Unit,
) {
    val bufOffs = bufOffsGetAndIncrement()
    value[bufOffs] = input

    // value.size == blockSize
    if ((bufOffs + 1) != value.size) return
    compress(value, 0)
    compressCountIncrement()
    bufOffsReset()
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Buffer.update(
    input: ByteArray,
    offset: Int,
    len: Int,
    bufOffsGet: () -> Int,
    bufOffsGetAndIncrement: () -> Int,
    bufOffsReset: () -> Unit,
    compress: (buf: ByteArray, offset: Int) -> Unit,
    compressCountIncrement: () -> Unit,
) {
    var i = offset
    var remaining = len

    // Fill buffer if not already empty
    while (bufOffsGet() != 0 && remaining > 0) {
        update(
            input[i++],
            bufOffsGetAndIncrement,
            bufOffsReset,
            compress,
            compressCountIncrement,
        )
        --remaining
    }

    // Chunk
    while (remaining >= value.size) {
        compress(input, i)
        i += value.size
        remaining -= value.size
        compressCountIncrement()
    }

    // Add remaining to buffer
    while (remaining-- > 0) {
        update(
            input[i++],
            bufOffsGetAndIncrement,
            bufOffsReset,
            compress,
            compressCountIncrement,
        )
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Buffer.digest(
    bufOffs: Int,
    compressCount: Long,
    digest: (bitLength: Long, offs: Int, buf: ByteArray) -> ByteArray,
    resetBufOffs: () -> Unit,
    resetCompressCount: () -> Unit,
    resetDigest: () -> Unit,
): ByteArray {
    val bitLength = ((compressCount * value.size) + bufOffs) * 8
    val final = digest(bitLength, bufOffs, value)
    reset(resetBufOffs, resetCompressCount, resetDigest)
    return final
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Buffer.reset(
    resetBufOffs: () -> Unit,
    resetCompressCount: () -> Unit,
    resetDigest: () -> Unit,
) {
    value.fill(0)
    resetBufOffs()
    resetCompressCount()
    resetDigest()
}
