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
@file:Suppress("KotlinRedundantDiagnosticSuppress")

package org.kotlincrypto.core.digest.internal

import org.kotlincrypto.core.digest.Digest
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Suppress("NOTHING_TO_INLINE")
internal inline fun Digest.commonToString(): String {
    return "Digest[${algorithm()}]@${hashCode()}"
}

@Throws(Exception::class)
@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
internal inline fun ByteArray.commonCheckArgs(
    offset: Int,
    len: Int,
    onShortInput: () -> Exception = { IllegalArgumentException("Input too short") },
    onOutOfBounds: (message: String) -> Exception = { IndexOutOfBoundsException(it) },
) {
    contract {
        callsInPlace(onShortInput, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onOutOfBounds, InvocationKind.AT_MOST_ONCE)
    }

    if (size - offset < len) throw onShortInput()
    if (offset < 0) throw onOutOfBounds("offset[$offset] < 0")
    if (len < 0) throw onOutOfBounds("len[$len] < 0")
    if (offset > size - len) throw onOutOfBounds("offset[$offset] > size[$size] - len[$len]")
}
