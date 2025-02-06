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
@file:Suppress("KotlinRedundantDiagnosticSuppress", "NOTHING_TO_INLINE")

package org.kotlincrypto.core.mac.internal

import org.kotlincrypto.core.mac.Mac
import org.kotlincrypto.error.ShortBufferException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private val SINGLE_0BYTE_KEY = ByteArray(1) { 0 }

@Suppress("UnusedReceiverParameter")
@Throws(IllegalArgumentException::class)
internal inline fun Mac.commonInit(algorithm: String) {
    require(algorithm.isNotBlank()) { "algorithm cannot be blank" }
}

internal inline fun Mac.commonToString(): String {
    return "Mac[${algorithm()}]@${hashCode()}"
}

@Throws(Exception::class)
@OptIn(ExperimentalContracts::class)
internal inline fun ByteArray.commonCheckArgs(
    offset: Int,
    len: Int,
    onShortInput: (message: String) -> Exception = ::IllegalArgumentException,
    onOutOfBounds: (message: String) -> Exception = ::IndexOutOfBoundsException,
) {
    contract {
        callsInPlace(onShortInput, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onOutOfBounds, InvocationKind.AT_MOST_ONCE)
    }

    if (size - offset < len) throw onShortInput("Too Short. size[$size] - offset[$offset] < len[$len]")
    if (offset < 0) throw onOutOfBounds("offset[$offset] < 0")
    if (len < 0) throw onOutOfBounds("len[$len] < 0")
    if (offset > size - len) throw onOutOfBounds("offset[$offset] > size[$size] - len[$len]")
}

@OptIn(ExperimentalContracts::class)
internal inline fun Mac.commonClearKey(engineReset: (ByteArray) -> Unit) {
    contract {
        callsInPlace(engineReset, InvocationKind.AT_LEAST_ONCE)
    }

    try {
        engineReset(SINGLE_0BYTE_KEY)
    } catch (e1: IllegalArgumentException) {
        try {
            engineReset(ByteArray(macLength()))
        } catch (e2: IllegalArgumentException) {
            e2.addSuppressed(e1)
            throw e2
        }
    }
}

@OptIn(ExperimentalContracts::class)
@Throws(IndexOutOfBoundsException::class, ShortBufferException::class)
internal inline fun Mac.commonDoFinalInto(
    dest: ByteArray,
    destOffset: Int,
    engineResetOnDoFinal: Boolean,
    engineDoFinalInto: (dest: ByteArray, destOffset: Int) -> Unit,
    engineReset: () -> Unit,
): Int {
    contract {
        callsInPlace(engineDoFinalInto, InvocationKind.AT_MOST_ONCE)
        callsInPlace(engineReset, InvocationKind.AT_MOST_ONCE)
    }

    val len = macLength()
    dest.commonCheckArgs(destOffset, len, onShortInput = ::ShortBufferException)
    engineDoFinalInto(dest, destOffset)
    if (engineResetOnDoFinal) engineReset()
    return len
}
