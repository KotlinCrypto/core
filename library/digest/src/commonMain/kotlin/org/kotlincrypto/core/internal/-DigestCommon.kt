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

package org.kotlincrypto.core.internal

import org.kotlincrypto.core.Digest
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Suppress("NOTHING_TO_INLINE")
internal inline fun Digest.commonToString(): String {
    return "Digest[${algorithm()}]@${hashCode()}"
}

@OptIn(ExperimentalContracts::class)
@Suppress("NOTHING_TO_INLINE")
@Throws(IllegalArgumentException::class, IndexOutOfBoundsException::class)
internal inline fun ByteArray.commonIfArgumentsValid(offset: Int, len: Int, block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    if (size - offset < len) throw IllegalArgumentException("Input too short")
    if (len == 0) return
    if (offset < 0 || len < 0 || offset > size - len) throw IndexOutOfBoundsException()

    block()
}
