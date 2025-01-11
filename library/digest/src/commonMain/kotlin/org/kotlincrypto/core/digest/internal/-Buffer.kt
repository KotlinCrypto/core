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
@file:Suppress("KotlinRedundantDiagnosticSuppress", "NOTHING_TO_INLINE")

package org.kotlincrypto.core.digest.internal

import org.kotlincrypto.core.digest.Digest
import kotlin.jvm.JvmInline

@JvmInline
internal value class Buffer internal constructor(internal val value: ByteArray)

@Throws(IllegalArgumentException::class)
@Suppress("UnusedReceiverParameter")
internal inline fun Digest.initializeBuffer(
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

internal inline fun Buffer.copy(): Buffer = Buffer(value.copyOf())

internal inline fun Buffer.commonUpdate(
    input: Byte,
    bufPosPlusPlus: Int,
    bufPosSet: (zero: Int) -> Unit,
    compressProtected: (ByteArray, Int) -> Unit,
) {
    val buf = value
    buf[bufPosPlusPlus] = input

    // buf.size == blockSize
    if ((bufPosPlusPlus + 1) != buf.size) return
    compressProtected(buf, 0)
    bufPosSet(0)
}

internal inline fun Buffer.commonUpdate(
    input: ByteArray,
    offset: Int,
    len: Int,
    bufPos: Int,
    bufPosSet: (value: Int) -> Unit,
    compressProtected: (ByteArray, Int) -> Unit,
) {
    val buf = value
    val blockSize = buf.size
    val limitInput = offset + len
    var posInput = offset
    var posBuf = bufPos

    if (posBuf > 0) {
        // Need to use buffered data (if possible)

        if (posBuf + len < blockSize) {
            // Not enough for a compression. Add it to the buffer.
            input.copyInto(buf, posBuf, posInput, limitInput)
            bufPosSet(posBuf + len)
            return
        }

        // Add enough input to do a compression
        val needed = blockSize - posBuf
        input.copyInto(buf, posBuf, posInput, posInput + needed)
        compressProtected(buf, 0)
        posBuf = 0
        posInput += needed
    }

    // Chunk blocks (if possible)
    while (posInput < limitInput) {
        val posNext = posInput + blockSize

        if (posNext > limitInput) {
            // Not enough for a compression. Add it to the buffer.
            input.copyInto(buf, 0, posInput, limitInput)
            posBuf = limitInput - posInput
            break
        }

        compressProtected(input, posInput)
        posInput = posNext
    }

    // Update globals
    bufPosSet(posBuf)
}

internal inline fun Buffer.commonDigest(
    input: ByteArray,
    updateProtected: (ByteArray, Int, Int) -> Unit,
    bufPosGet: () -> Int,
    digestProtected: (buf: ByteArray, bufPos: Int) -> ByteArray,
    resetProtected: () -> Unit,
    bufPosSet: (zero: Int) -> Unit,
): ByteArray {
    updateProtected(input, 0, input.size)
    return commonDigest(bufPosGet(), digestProtected, resetProtected, bufPosSet)
}

internal inline fun Buffer.commonDigest(
    bufPos: Int,
    digestProtected: (buf: ByteArray, bufPos: Int) -> ByteArray,
    resetProtected: () -> Unit,
    bufPosSet: (zero: Int) -> Unit,
): ByteArray {
    // Zeroize any stale input that may be left in the buffer
    value.fill(0, bufPos)
    val digest = digestProtected(value, bufPos)
    commonReset(resetProtected, bufPosSet)
    return digest
}

internal inline fun Buffer.commonReset(
    resetProtected: () -> Unit,
    bufPosSet: (zero: Int) -> Unit,
) {
    value.fill(0)
    bufPosSet(0)
    resetProtected()
}
