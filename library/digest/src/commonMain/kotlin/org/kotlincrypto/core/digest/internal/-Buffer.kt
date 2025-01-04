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

    internal fun copy(): Buffer = Buffer(value.copyOf())

    internal companion object {

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
internal inline fun Buffer.commonUpdate(
    input: Byte,
    bufPosPlusPlus: Int,
    bufPosSet: (zero: Int) -> Unit,
    compressProtected: (buf: ByteArray, offset: Int) -> Unit,
) {
    val buf = value
    buf[bufPosPlusPlus] = input

    // buf.size == blockSize
    if ((bufPosPlusPlus + 1) != buf.size) return
    compressProtected(buf, 0)
    bufPosSet(0)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun Buffer.commonUpdate(
    input: ByteArray,
    offset: Int,
    len: Int,
    bufPos: Int,
    bufPosSet: (value: Int) -> Unit,
    compressProtected: (buf: ByteArray, offset: Int) -> Unit,
) {
    val buf = value
    val blockSize = buf.size
    var offsInput = offset
    val limit = offsInput + len
    var offsBuf = bufPos

    if (offsBuf > 0) {
        // Need to use buffered data (if possible)

        if (offsBuf + len < blockSize) {
            // Not enough for a compression. Add it to the buffer.
            input.copyInto(buf, offsBuf, offsInput, limit)
            bufPosSet(offsBuf + len)
            return
        }

        // Add enough input to do a compression
        val needed = blockSize - offsBuf
        input.copyInto(buf, offsBuf, offsInput, offsInput + needed)
        compressProtected(buf, 0)
        offsBuf = 0
        offsInput += needed
    }

    // Chunk blocks (if possible)
    while (offsInput < limit) {
        val offsNext = offsInput + blockSize

        if (offsNext > limit) {
            // Not enough for a compression. Add it to the buffer.
            input.copyInto(buf, 0, offsInput, limit)
            offsBuf = limit - offsInput
            break
        }

        compressProtected(input, offsInput)
        offsInput = offsNext
    }

    // Update globals
    bufPosSet(offsBuf)
}
